package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.Populatable;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.Autopopulator;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import org.fxmisc.easybind.EasyBind;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.application.Platform;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;

import static edu.wpi.first.shuffleboard.api.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  /**
   * Creates a dashboard with one default tab.
   */
  public DashboardTabPane() {
    this(createAutoPopulateTab("SmartDashboard", "network_table:///SmartDashboard/"),
        createAutoPopulateTab("LiveWindow", "network_table:///LiveWindow/"));
  }

  private static DashboardTab createAutoPopulateTab(String name, String sourcePrefix) {
    DashboardTab tab = new DashboardTab(name);
    tab.setAutoPopulate(true);
    tab.setSourcePrefix(sourcePrefix);
    return tab;
  }

  /**
   * Create a dashboard with the given tabs.
   */
  public DashboardTabPane(Tab... tabs) {
    super(tabs);
    getStyleClass().add("dashboard-tabs");
    AdderTab adder = new AdderTab();
    adder.setAddTabCallback(this::addNewTab);
    getTabs().add(adder);
  }

  /**
   * Adds a new tab at the end of the tab list. The default name is "Tab <i>n</i>", where <i>n</i> is the number
   * of dashboard tabs in the pane.
   *
   * @return the newly created tab
   */
  public DashboardTab addNewTab() {
    int existingTabs = getTabs().size();
    DashboardTab tab = new DashboardTab("Tab " + existingTabs);
    if (existingTabs > 0) {
      getTabs().add(existingTabs - 1, tab);
    } else {
      getTabs().add(tab);
    }
    return tab;
  }

  /**
   * Selects the tab with the given index. If the index is negative or larger than the highest index, no tab is
   * selected.
   *
   * @param tabIndex the index of the tab to select
   */
  public void selectTab(int tabIndex) {
    if (tabIndex >= 0 && tabIndex < getTabs().size() && getTabs().get(tabIndex) instanceof DashboardTab) {
      getSelectionModel().select(tabIndex);
    }
  }

  /**
   * Closes the currently selected tab.
   */
  public void closeCurrentTab() {
    getTabs().remove(getSelectionModel().getSelectedItem());
  }

  /**
   * Add a component to the active tab pane.
   * Should only be done by the result of specific user interaction.
   */
  public void addComponentToActivePane(Component widget) {
    optionalCast(getSelectionModel().getSelectedItem(), DashboardTab.class)
        .ifPresent(tab -> tab.getWidgetPane().addComponent(widget));
  }

  /**
   * Highlights widgets matching a predicate on all tabs.
   */
  public void selectWidgets(Predicate<Widget> selector) {
    getTabs().stream().map(optionalCast(DashboardTab.class))
        .forEach(tab -> tab.map(DashboardTab::getWidgetPane)
            .ifPresent(pane -> pane.selectWidgets(selector)));
  }

  public static class DashboardTab extends Tab implements HandledTab, Populatable {
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

    private boolean deferPopulation = true;

    /**
     * Creates a single dashboard tab with the given title.
     */
    public DashboardTab(String title) {
      super();
      this.title.set(title);
      setGraphic(new TabHandle(this));

      widgetPane.addListener((__, prev, cur) -> {
        cur.getTiles().addListener((ListChangeListener<Tile>) c -> {
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
        });
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

      setContextMenu(new ContextMenu(FxUtils.menuItem("Preferences", __ -> showPrefsDialog())));
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
              getWidgetPane().tileSizeProperty()
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
      if (getWidgetPane().getScene() == null || getWidgetPane().getParent() == null) {
        // Defer until the pane is visible and is laid out in the scene
        deferPopulation = true;
        Platform.runLater(this::populate);
        return;
      }
      if (deferPopulation) {
        // Defer one last time; this method tends to trigger before row/column bindings on the widget pane
        // This makes sure the pane is properly sized before populating it
        deferPopulation = false;
        Platform.runLater(this::populate);
      }
      Autopopulator.getDefault().populate(this);
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
    public boolean supports(DataSource<?> source) {
      return !deferPopulation
          && isAutoPopulate()
          && source.getDataType() != DataTypes.Map
          && (source.getName().startsWith(getSourcePrefix()) || source.getId().startsWith(getSourcePrefix()));
    }

    @Override
    public boolean hasComponentFor(DataSource<?> source) {
      return getWidgetPane().getTiles().stream()
          .map(Tile::getContent)
          .flatMap(TypeUtils.castStream(Sourced.class))
          .anyMatch(s -> s.getSource().equals(source)
              || (source.getId().startsWith(s.getSource().getId()) && !(s instanceof ComponentContainer)));
    }

    @Override
    public void addComponentFor(DataSource<?> source) {
      List<Populatable> targets = getWidgetPane().components()
          .flatMap(TypeUtils.castStream(Populatable.class))
          .filter(p -> p.supports(source))
          .collect(Collectors.toList());

      if (targets.isEmpty()) {
        // No nested components capable of adding a component for the source, add it to the root widget pane
        Components.getDefault().defaultComponentNameFor(source.getDataType())
            .flatMap(s -> Components.getDefault().createComponent(s, source))
            .ifPresent(c -> {
              c.setTitle(source.getName());
              getWidgetPane().addComponent(c);
              if (c instanceof Populatable) {
                Autopopulator.getDefault().addTarget((Populatable) c);
              }
            });
      } else {
        // Add a component everywhere possible
        targets.forEach(t -> t.addComponentIfPossible(source));
      }
    }
  }
}
