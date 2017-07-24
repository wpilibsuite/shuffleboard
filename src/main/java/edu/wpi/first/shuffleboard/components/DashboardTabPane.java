package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.WidgetPropertySheet;

import org.controlsfx.control.PropertySheet;
import org.fxmisc.easybind.EasyBind;

import java.util.Arrays;
import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import static edu.wpi.first.shuffleboard.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  /**
   * Creates a dashboard with one default tab.
   */
  public DashboardTabPane() {
    this(new DashboardTab("Tab 1"));
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

  private DashboardTab addNewTab() {
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

    /**
     * Creates a single dashboard tab with the given title.
     */
    public DashboardTab(String title) {
      super();
      this.title.set(title);
      setGraphic(new TabHandle(this));

      setWidgetPane(new WidgetPane());
      this.contentProperty().bind(widgetPane);
      setContextMenu(new ContextMenu(FxUtils.menuItem("Preferences", e -> {
        PropertySheet propertySheet = new WidgetPropertySheet(
            Arrays.asList(
                this.title,
                getWidgetPane().tileSizeProperty(),
                getWidgetPane().hgapProperty(),
                getWidgetPane().vgapProperty()
            ));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.titleProperty().bind(EasyBind.map(this.title, t -> "Preferences for " + t));
        dialog.getDialogPane().setContent(propertySheet);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
      })));
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
  }
}
