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

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.fxmisc.easybind.EasyBind;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Labeled;
import javafx.scene.control.TabPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class LeftDrawerController {

  private static final GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
  private static final Glyph EXPAND = fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_RIGHT);
  private static final Glyph CONTRACT = fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_LEFT);

  @FXML
  private Pane root;
  @FXML
  private TabPane tabs;
  @FXML
  private Accordion sourcesAccordion;
  @FXML
  private WidgetGallery widgetGallery;
  @FXML
  private Node handle;
  @FXML
  private Labeled expandContractButton;

  private final Multimap<Plugin, TitledPane> sourcePanes = ArrayListMultimap.create();
  private final BooleanProperty expanded = new SimpleBooleanProperty(true);

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
    expanded.addListener((__, was, expanded) -> {
      if (root.getScene() == null) {
        // Not in a scene; don't bother animating
        if (expanded) {
          tabs.setMaxWidth(tabs.getPrefWidth());
        } else {
          tabs.setMaxWidth(0);
        }
        return;
      }
      Timeline timeline = new Timeline(60);
      double target = expanded ? tabs.getPrefWidth() : 0;
      timeline.getKeyFrames().add(
          new KeyFrame(Duration.millis(150), new KeyValue(tabs.maxWidthProperty(), target))
      );
      timeline.playFromStart();
    });
    handle.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2) {
        toggleView();
      }
    });
    expandContractButton.graphicProperty().bind(EasyBind.monadic(expanded).map(expanded -> {
      if (expanded) {
        return CONTRACT;
      } else {
        return EXPAND;
      }
    }));
    Tooltip expandContractTooltip = new Tooltip();
    expandContractTooltip.textProperty().bind(EasyBind.monadic(expanded).map(expanded -> {
      if (expanded) {
        return "Collapse the drawer";
      } else {
        return "Expand the drawer";
      }
    }));
    expandContractButton.setTooltip(expandContractTooltip);
    expanded.set(false);
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
            new InteractiveSourceTree(
                sourceType,
                t -> addComponentToActivePane.accept(t),
                t -> createTabForSource.accept(t)
            );

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

  @FXML
  private void toggleView() {
    expanded.set(!expanded.get());
  }

  public void hide() {
    expanded.set(false);
  }

  public void show() {
    expanded.set(true);
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
