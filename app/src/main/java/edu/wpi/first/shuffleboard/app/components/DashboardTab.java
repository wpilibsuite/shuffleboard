package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.Populatable;
import edu.wpi.first.shuffleboard.api.components.WidgetPropertySheet;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.ComponentInstantiationException;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.app.Autopopulator;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import edu.wpi.first.networktables.NetworkTable;

import org.fxmisc.easybind.EasyBind;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.BorderPane;

public class DashboardTab extends Tab implements HandledTab, Populatable {

  private static final Logger log = Logger.getLogger(DashboardTab.class.getName());

  private final ObjectProperty<WidgetPane> widgetPane = new SimpleObjectProperty<>(this, "widgetPane");
  private final StringProperty title = new SimpleStringProperty(this, "title", "");
  private final BooleanProperty autoPopulate = new SimpleBooleanProperty(this, "autoPopulate", false);
  private final StringProperty sourcePrefix = new SimpleStringProperty(this, "sourcePrefix", "");

  /**
   * Debounces populate() calls so we don't freeze the app while a source type is doing its initial discovery of
   * available source URIs. Not debouncing makes typical application startup take at least 5 seconds on an i7-6700HQ
   * where the user sees nothing but a blank screen - no UI elements or anything!
   *
   * <p>Note that this is only used when we manually force a population call, which is only required because the
   * filtering criteria for compatible sources changes based on the {@code sourcePrefix} property.
   */
  private final Debouncer populateDebouncer =
      new Debouncer(() -> FxUtils.runOnFxThread(this::populate), Duration.ofMillis(50));

  private final ListChangeListener<Tile> tileListChangeListener = c -> {
    while (c.next()) {
      if (c.wasAdded()) {
        c.getAddedSubList().stream()
            .map(Tile::getContent)
            .flatMap(TypeUtils.castStream(Populatable.class))
            .collect(Collectors.toList())
            .forEach(Autopopulator.getDefault()::addTarget);
      } else if (c.wasRemoved()) {
        c.getRemoved().stream()
            .map(Tile::getContent)
            .flatMap(TypeUtils.castStream(Populatable.class))
            .collect(Collectors.toList())
            .forEach(Autopopulator.getDefault()::removeTarget);
      }
    }
  };

  private boolean deferPopulation = true;

  /**
   * Creates a single dashboard tab with the given title.
   */
  public DashboardTab(String title) {
    super();
    this.title.set(title);
    setGraphic(new TabHandle(this));

    widgetPane.addListener((__, prev, cur) -> {
      if (prev != null) {
        prev.getTiles().removeListener(tileListChangeListener);
      }
      if (cur != null) {
        cur.getTiles().addListener(tileListChangeListener);
      }
    });

    setWidgetPane(new WidgetPane());

    this.contentProperty().bind(widgetPane);

    autoPopulate.addListener((__, was, is) -> {
      if (is) {
        Autopopulator.getDefault().addTarget(this);
      } else {
        Autopopulator.getDefault().removeTarget(this);
      }
    });
    autoPopulate.addListener(__ -> populateDebouncer.run());
    sourcePrefix.addListener(__ -> populateDebouncer.run());

    MenuItem prefItem = FxUtils.menuItem("Preferences", __ -> showPrefsDialog());
    prefItem.setStyle("-fx-text-fill: black;");
    setContextMenu(new ContextMenu(prefItem));
  }

  /**
   * Shows a dialog for editing the properties of this tab.
   */
  public void showPrefsDialog() {
    // Use a dummy property here to prevent a call to populate() on every keystroke in the editor (!)
    StringProperty dummySourcePrefix
        = new SimpleStringProperty(sourcePrefix.getBean(), sourcePrefix.getName(), sourcePrefix.getValue());
    WidgetPropertySheet propertySheet = new WidgetPropertySheet(
        Arrays.asList(
            this.title,
            this.autoPopulate,
            dummySourcePrefix,
            getWidgetPane().tileSizeProperty(),
            getWidgetPane().showGridProperty()
        ));
    propertySheet.getItems().addAll(
        new WidgetPropertySheet.PropertyItem<>(getWidgetPane().hgapProperty(), "Horizontal spacing"),
        new WidgetPropertySheet.PropertyItem<>(getWidgetPane().vgapProperty(), "Vertical spacing")
    );
    Dialog<ButtonType> dialog = new Dialog<>();
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    dialog.setResizable(true);
    dialog.titleProperty().bind(EasyBind.map(this.title, t -> t + " Preferences"));
    dialog.getDialogPane().setContent(new BorderPane(propertySheet));
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
    dialog.setOnCloseRequest(__ -> {
      this.sourcePrefix.setValue(dummySourcePrefix.getValue());
    });
    dialog.showAndWait();
  }

  /**
   * Populates this tab with all available sources that begin with the set source prefix and don't already have a
   * widget to display it or any higher-level source.
   */
  private void populate() {
    if (getTabPane() == null) {
      // No longer in the scene; bail
      return;
    }
    if (getWidgetPane().getScene() == null || getWidgetPane().getParent() == null) {
      // Defer until the pane is visible and is laid out in the scene
      deferPopulation = true;
      populateDebouncer.run();
      return;
    }
    if (deferPopulation) {
      // Defer one last time; this method tends to trigger before row/column bindings on the widget pane
      // This makes sure the pane is properly sized before populating it
      deferPopulation = false;
      populateDebouncer.run();
      return;
    }
    if (isAutoPopulate()) {
      Autopopulator.getDefault().populate(this);
    }
  }

  public WidgetPane getWidgetPane() {
    return widgetPane.get();
  }

  public ObjectProperty<WidgetPane> widgetPaneProperty() {
    return widgetPane;
  }

  public void setWidgetPane(WidgetPane widgetPane) {
    this.widgetPane.set(widgetPane);
  }

  Debouncer getPopulateDebouncer() {
    return populateDebouncer;
  }

  @Override
  public Tab getTab() {
    return this;
  }

  @Override
  public StringProperty titleProperty() {
    return title;
  }

  public String getTitle() {
    return title.get();
  }

  public void setTitle(String title) {
    this.title.set(title);
  }

  public boolean isAutoPopulate() {
    return autoPopulate.get();
  }

  public BooleanProperty autoPopulateProperty() {
    return autoPopulate;
  }

  public void setAutoPopulate(boolean autoPopulate) {
    this.autoPopulate.set(autoPopulate);
  }

  public String getSourcePrefix() {
    return sourcePrefix.get();
  }

  public StringProperty sourcePrefixProperty() {
    return sourcePrefix;
  }

  public void setSourcePrefix(String sourceRegex) {
    this.sourcePrefix.set(sourceRegex);
  }

  @Override
  public boolean supports(String sourceId) {
    SourceType type = SourceTypes.getDefault().typeForUri(sourceId);
    String name = NetworkTable.normalizeKey(type.removeProtocol(sourceId), false);
    return !deferPopulation
        && isAutoPopulate()
        && type.dataTypeForSource(DataTypes.getDefault(), sourceId) != DataTypes.Map
        && !NetworkTableUtils.isMetadata(sourceId)
        && (name.startsWith(getSourcePrefix()) || sourceId.startsWith(getSourcePrefix()));
  }

  @Override
  public boolean hasComponentFor(String sourceId) {
    return getWidgetPane().getTiles().stream()
        .map(Tile::getContent)
        .flatMap(TypeUtils.castStream(Sourced.class))
        .anyMatch(s -> s.getSources().stream().map(DataSource::getId).anyMatch(sourceId::equals)
            || (s.getSources().stream().map(DataSource::getId).anyMatch(sourceId::startsWith)
            && !(s instanceof ComponentContainer)));
  }

  @Override
  public void addComponentFor(DataSource<?> source) {
    List<Populatable> targets = getWidgetPane().components()
        .flatMap(TypeUtils.castStream(Populatable.class))
        .filter(p -> p.supports(source.getId()))
        .collect(Collectors.toList());

    if (targets.isEmpty()) {
      // No nested components capable of adding a component for the source, add it to the root widget pane
      try {
        Components.getDefault().defaultComponentNameFor(source.getDataType())
            .flatMap(s -> Components.getDefault().createComponent(s, source))
            .ifPresent(c -> {
              c.setTitle(source.getName());
              getWidgetPane().addComponent(c);
              if (c instanceof Populatable) {
                Autopopulator.getDefault().addTarget((Populatable) c);
              }
            });
      } catch (ComponentInstantiationException e) {
        log.log(Level.WARNING, "Could not add component for source " + source.getId(), e);
      }
    } else {
      // Add a component everywhere possible
      targets.forEach(t -> t.addComponentIfPossible(source));
    }
  }
}
