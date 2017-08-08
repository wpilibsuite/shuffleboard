package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.NetworkTableSourceType;
import edu.wpi.first.shuffleboard.app.widget.WidgetPropertySheet;
import edu.wpi.first.shuffleboard.app.widget.Widgets;

import org.fxmisc.easybind.EasyBind;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

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

import static edu.wpi.first.shuffleboard.app.util.TypeUtils.optionalCast;

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
    private static final ObservableList<String> availableSourceIds =
        NetworkTableSourceType.INSTANCE.getAvailableSourceIds();

    /**
     * Creates a single dashboard tab with the given title.
     */
    public DashboardTab(String title) {
      super();
      this.title.set(title);
      setGraphic(new TabHandle(this));

      setWidgetPane(new WidgetPane());

      // UGLY HACK, REMOVE (makes autopopulation work)
      getWidgetPane().setNumColumns(10);
      getWidgetPane().setNumRows(6);

      this.contentProperty().bind(widgetPane);

      autoPopulate.addListener(__ -> populate());
      sourcePrefix.addListener(__ -> populate());
      availableSourceIds.addListener((ListChangeListener<String>) c -> populate());

      setContextMenu(new ContextMenu(FxUtils.menuItem("Preferences", e -> {
        WidgetPropertySheet propertySheet = new WidgetPropertySheet(
            Arrays.asList(
                this.title,
                this.autoPopulate,
                this.sourcePrefix,
                getWidgetPane().tileSizeProperty(),
                getWidgetPane().hgapProperty(),
                getWidgetPane().vgapProperty()
            ));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setResizable(true);
        dialog.titleProperty().bind(EasyBind.map(this.title, t -> "Preferences for " + t));
        dialog.getDialogPane().setContent(propertySheet);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
      })));
    }

    private void populate() {
      if (!isAutoPopulate() || getSourcePrefix().isEmpty()) {
        return;
      }
      availableSourceIds.stream()
          .filter(id -> id.startsWith(getSourcePrefix())
              || SourceTypes.stripProtocol(id).startsWith(getSourcePrefix()))
          .filter(id -> SourceTypes.typeForUri(id) != NetworkTableSourceType.INSTANCE
              || !NetworkTableUtils.isMetadata(NetworkTableSourceType.INSTANCE.removeProtocol(id)))
          .filter(id -> getWidgetPane().getTiles().stream()
              .map(t -> t.getWidget().getSource())
              .noneMatch(s -> id.startsWith(s.getId())))
          .sorted()
          .map(SourceTypes::forUri)
          .filter(s -> s.getDataType() != DataTypes.Unknown) // Don't create widgets for the catchall types
          .filter(s -> s.getDataType() != DataTypes.Map)
          .filter(s -> !Widgets.widgetNamesForSource(s).isEmpty())
          .map(s -> Widgets.createWidget(Widgets.widgetNamesForSource(s).get(0), s))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .forEach(w -> getWidgetPane().addWidget(w));
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
