package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Widgets;

import org.fxmisc.easybind.EasyBind;

import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import static edu.wpi.first.shuffleboard.api.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  /**
   * Creates a dashboard with one default tab.
   */
  public DashboardTabPane() {
    this(createAutoPopulateTab("SmartDashboard", "/SmartDashboard/"),
        createAutoPopulateTab("LiveWindow", "/LiveWindow/"));
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
   * Add a widget to the active tab pane.
   * Should only be done by the result of specific user interaction.
   */
  public void addWidgetToActivePane(Widget widget) {
    optionalCast(getSelectionModel().getSelectedItem(), DashboardTab.class)
        .ifPresent(tab -> tab.getWidgetPane().addWidget(widget));
  }

  /**
   * Highlights widgets matching a predicate on all tabs.
   */
  public void selectWidgets(Predicate<Widget> selector) {
    getTabs().stream().map(optionalCast(DashboardTab.class))
        .forEach(tab -> tab.map(DashboardTab::getWidgetPane)
            .ifPresent(pane -> pane.selectWidgets(selector)));
  }

  public static class DashboardTab extends Tab implements HandledTab {
    private final ObjectProperty<WidgetPane> widgetPane = new SimpleObjectProperty<>(this, "widgetPane");
    private final StringProperty title = new SimpleStringProperty(this, "title", "");
    private final BooleanProperty autoPopulate = new SimpleBooleanProperty(this, "autoPopulate", false);
    private final StringProperty sourcePrefix = new SimpleStringProperty(this, "sourcePrefix", "");
    private static final ObservableList<String> availableSourceIds = SourceTypes.getDefault().allAvailableSourceUris();

    /**
     * Debounces populate() calls so we don't freeze the app while a source type is doing its initial discovery of
     * available source URIs. Not debouncing makes typical application startup take at least 5 seconds on an i7-6700HQ
     * where the user sees nothing but a blank screen - no UI elements or anything!
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

      setWidgetPane(new WidgetPane());

      this.contentProperty().bind(widgetPane);

      autoPopulate.addListener(__ -> populateDebouncer.run());
      sourcePrefix.addListener(__ -> populateDebouncer.run());
      availableSourceIds.addListener((ListChangeListener<String>) c -> populateDebouncer.run());

      setContextMenu(new ContextMenu(FxUtils.menuItem("Preferences", e -> {
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
        dialog.setResizable(true);
        dialog.titleProperty().bind(EasyBind.map(this.title, t -> t + " Preferences"));
        dialog.getDialogPane().setContent(propertySheet);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.setOnCloseRequest(__ -> {
          this.sourcePrefix.setValue(dummySourcePrefix.getValue());
        });
        dialog.showAndWait();
      })));
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
      if (!isAutoPopulate() || getSourcePrefix().isEmpty()) {
        return;
      }
      for (String id : availableSourceIds) {
        if (shouldAutopopulate(id)
            && noExistingWidgetsForSource(id)) {
          DataSource<?> source = SourceTypes.getDefault().forUri(id);

          // Don't create widgets for the catchall types
          if (source.getDataType() != DataTypes.Unknown
              && source.getDataType() != DataTypes.Map
              && !Widgets.getDefault().widgetNamesForSource(source).isEmpty()) {
            Widgets.getDefault().createWidget(Widgets.getDefault().widgetNamesForSource(source).get(0), source)
                .ifPresent(w -> getWidgetPane().addWidget(w));
          }
        }
      }
    }

    private boolean shouldAutopopulate(String sourceId) {
      return sourceId.startsWith(getSourcePrefix())
          || SourceTypes.getDefault().stripProtocol(sourceId).startsWith(getSourcePrefix());
    }

    /**
     * Checks if there are any widgets in the widget pane backed by a source with the given ID.
     *
     * @param id the ID of the source to check for
     */
    private boolean noExistingWidgetsForSource(String id) {
      return getWidgetPane().getTiles().stream()
          .flatMap(t -> t.getContent().allWidgets())
          .map(Widget::getSource)
          .noneMatch(s -> id.startsWith(s.getId()));
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
  }
}
