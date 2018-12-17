package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.PropertyParsers;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.tab.model.StructureChangeListener;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabStructure;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.event.Event;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import static edu.wpi.first.shuffleboard.api.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  private final Map<TabStructure, Map<TabModel, ProcedurallyDefinedTab>> pluginTabs = new WeakHashMap<>();

  private final StructureChangeListener structureChangeListener = tabs -> {
    FxUtils.runOnFxThread(() -> {
      Map<TabModel, ProcedurallyDefinedTab> realTabs = pluginTabs.computeIfAbsent(tabs, __ -> new WeakHashMap<>());
      for (TabModel model : tabs.getTabs().values()) {
        var tab = realTabs.computeIfAbsent(model, m -> new ProcedurallyDefinedTab(m, PropertyParsers.getDefault()));
        if (!getTabs().contains(tab)) {
          getTabs().add(getTabs().size() - 1, tab);
        }
        tab.populate();
      }

      if (tabs.getSelectedTabIndex() >= 0) {
        selectTab(tabs.getSelectedTabIndex());
      } else if (tabs.getSelectedTabTitle() != null) {
        String title = tabs.getSelectedTabTitle();
        getTabs()
            .stream()
            .flatMap(TypeUtils.castStream(DashboardTab.class))
            .filter(t -> title.equals(t.getTitle()))
            .findFirst()
            .ifPresent(getSelectionModel()::select);
      }
    });
  };

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
    PluginLoader.getDefault().getLoadedPlugins()
        .stream()
        .map(Plugin::getTabs)
        .filter(Objects::nonNull)
        .forEach(structureChangeListener::structureChanged);

    PluginLoader.getDefault().getLoadedPlugins()
        .stream()
        .map(Plugin::getTabs)
        .filter(Objects::nonNull)
        .forEach(s -> s.addStructureChangeListener(structureChangeListener));

    PluginLoader.getDefault().getLoadedPlugins().addListener((SetChangeListener<Plugin>) change -> {
      if (change.wasAdded()) {
        TabStructure customTabs = change.getElementAdded().getTabs();
        if (customTabs != null) {
          customTabs.addStructureChangeListener(structureChangeListener);
        }
      } else if (change.wasRemoved()) {
        TabStructure customTabs = change.getElementRemoved().getTabs();
        if (customTabs != null) {
          customTabs.removeStructureChangeListener(structureChangeListener);
        }
      }
    });
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
    if (tab.getOnCloseRequest() != null) { // NOPMD
      tab.getOnCloseRequest().handle(new Event(this, tab, Tab.TAB_CLOSE_REQUEST_EVENT));
    } else {
      getTabs().remove(tab);
    }
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
