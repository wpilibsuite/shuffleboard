package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.function.Predicate;

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

    /**
     * Creates a single dashboard tab with the given title.
     */
    public DashboardTab(String title) {
      super();
      this.title.set(title);
      setGraphic(new TabHandle(this));

      setWidgetPane(new WidgetPane());
      this.contentProperty().bind(widgetPane);
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
