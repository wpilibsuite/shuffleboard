package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.app.components.InteractiveSourceTree;
import edu.wpi.first.shuffleboard.app.components.WidgetGallery;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;

public class LeftDrawerController {

  @FXML
  private Pane root;
  @FXML
  private Accordion sourcesAccordion;
  @FXML
  private WidgetGallery widgetGallery;

  private final Multimap<Plugin, TitledPane> sourcePanes = ArrayListMultimap.create();

  // TODO inject these
  private Consumer<Component> addComponentToActivePane;
  private Consumer<SourceEntry> createTabForSource;

  @FXML
  private void initialize() {
    PluginLoader.getDefault().getLoadedPlugins().forEach(plugin -> {
      listenToPluginChanges(plugin);
      setup(plugin);
    });
    sourcesAccordion.getPanes().sort(Comparator.comparing(TitledPane::getText));
    PluginLoader.getDefault().getKnownPlugins().addListener((ListChangeListener<Plugin>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(plugin -> {
            listenToPluginChanges(plugin);
            setup(plugin);
          });
        }
        sourcesAccordion.getPanes().sort(Comparator.comparing(TitledPane::getText));
      }
    });
    FxUtils.setController(root, this);
  }

  private void listenToPluginChanges(Plugin plugin) {
    plugin.loadedProperty().addListener((__, was, is) -> {
      if (is) {
        setup(plugin);
      } else {
        tearDown(plugin);
      }
    });
  }

  /**
   * Sets up UI components to represent the sources that a plugin defines.
   */
  private void setup(Plugin plugin) {
    FxUtils.runOnFxThread(() -> {
      plugin.getSourceTypes().forEach(sourceType -> {
        InteractiveSourceTree tree =
            new InteractiveSourceTree(sourceType, addComponentToActivePane, createTabForSource);

        TitledPane titledPane = new TitledPane(sourceType.getName(), tree);
        sourcePanes.put(plugin, titledPane);
        sourcesAccordion.getPanes().add(titledPane);
        FXCollections.sort(sourcesAccordion.getPanes(), Comparator.comparing(TitledPane::getText));
        if (sourcesAccordion.getExpandedPane() == null) {
          sourcesAccordion.setExpandedPane(titledPane);
        }
      });

      // Add widgets to the gallery as well
      widgetGallery.setWidgets(Components.getDefault().allWidgets().collect(Collectors.toList()));
    });
  }

  /**
   * Removes all traces from a plugin from the left drawer. Source trees will be removed and all widgets
   * defined by the plugin will be removed from the gallery.
   */
  private void tearDown(Plugin plugin) {
    // Remove the source panes
    sourcesAccordion.getPanes().removeAll(sourcePanes.removeAll(plugin));
    FXCollections.sort(sourcesAccordion.getPanes(), Comparator.comparing(TitledPane::getText));

    // Remove widgets from the gallery
    widgetGallery.setWidgets(Components.getDefault().allWidgets().collect(Collectors.toList()));
  }

  // TODO inject at initialization
  public void setAddComponentToActivePane(Consumer<Component> addComponentToActivePane) {
    this.addComponentToActivePane = addComponentToActivePane;
  }

  // TODO inject at initialization
  public void setCreateTabForSource(Consumer<SourceEntry> createTabForSource) {
    this.createTabForSource = createTabForSource;
  }
}
