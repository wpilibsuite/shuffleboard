package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.function.Predicate;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import static edu.wpi.first.shuffleboard.api.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  public DashboardTabPane() {
  }

  /**
   * Creates a dashboard tab that will auto populate for the given source prefix.
   *
   * @param name         the name of the tab
   * @param sourcePrefix the prefixes to sources to populate the tab with
   */
  public static DashboardTab createAutoPopulateTab(String name, String sourcePrefix) {
    DashboardTab tab = new DashboardTab(name);
    tab.setAutoPopulate(true);
    tab.setSourcePrefix(sourcePrefix);
    return tab;
  }

  /**
   * Creates a dashboard tab pane with two autopopulating tabs for FRC use: "SmartDashboard" and "LiveWindow".
   */
  // TODO move this to the FRC plugin. This doesn't belong here
  public static DashboardTabPane createDefaultFrcPane() {
    return new DashboardTabPane(
        createAutoPopulateTab("SmartDashboard", "SmartDashboard/"),
        createAutoPopulateTab("LiveWindow", "LiveWindow/")
    );
  }

  /**
   * Create a dashboard with the given tabs.
   */
  public DashboardTabPane(Tab... tabs) {
    super(tabs);
    getTabs().addListener(DashboardTabPane::onTabsChanged);
    getStyleClass().add("dashboard-tabs");
    AdderTab adder = new AdderTab();
    adder.setAddTabCallback(this::addNewTab);
    getTabs().add(adder);
  }

  private static void onTabsChanged(ListChangeListener.Change<? extends Tab> change) {
    while (change.next()) {
      if (change.wasRemoved()) {
        change.getRemoved().stream()
            .flatMap(TypeUtils.castStream(DashboardTab.class))
            .map(DashboardTab::getPopulateDebouncer)
            .forEach(Debouncer::cancel);
      }
    }
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

}
