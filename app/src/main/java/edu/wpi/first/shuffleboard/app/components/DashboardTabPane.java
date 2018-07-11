package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import static edu.wpi.first.shuffleboard.api.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  /**
   * Creates a dashboard with no tabs.
   */
  public DashboardTabPane() {
    this((Tab[]) null);
  }

  public DashboardTabPane(Collection<TabInfo> tabInfo) {
    this(tabInfo.stream().map(DashboardTab::new).toArray(DashboardTab[]::new));
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
            .forEach(tab -> {
              tab.getPopulateDebouncer().cancel();
              WidgetPane widgetPane = tab.getWidgetPane();
              List<Tile> tiles = new ArrayList<>(widgetPane.getTiles());
              tiles.stream()
                  .map((Function<Tile, Component>) widgetPane::removeTile)
                  .flatMap(c -> c.allComponents())
                  .flatMap(TypeUtils.castStream(Sourced.class))
                  .forEach(Sourced::removeAllSources);
            });
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
    Tab tab = getSelectionModel().getSelectedItem();
    Event event = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
    Event.fireEvent(tab, event);
    getTabs().remove(tab);
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

  /**
   * Creates a new tab to autopopulate with data in a complex data source, then selects that tab.
   *
   * @param entry the source entry to create a tab for
   */
  public void createTabForSource(SourceEntry entry) {
    DataSource<?> source = entry.get();
    if (!source.getDataType().isComplex()) {
      throw new IllegalArgumentException("Data source does not provide complex data");
    }
    DashboardTab newTab = new DashboardTab(entry.getViewName());
    getTabs().add(getTabs().size() - 1, newTab);
    newTab.setSourcePrefix(source.getId() + "/");
    newTab.setAutoPopulate(true);
    getSelectionModel().select(newTab);
  }
}
