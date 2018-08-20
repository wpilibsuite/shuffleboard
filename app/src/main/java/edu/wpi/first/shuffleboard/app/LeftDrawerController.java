package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.components.TreeItemPredicate;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.StringUtils;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Labeled;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

public final class LeftDrawerController {

  private static final GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
  private static final Glyph EXPAND = fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_RIGHT);
  private static final Glyph CONTRACT = fontAwesome.create(FontAwesome.Glyph.ANGLE_DOUBLE_LEFT);

  public static final double MAX_WIDTH = 800;
  public static final Object EXPANDED_SIZE_KEY = new Object();

  @FXML
  private Pane root;
  @FXML
  private TabPane tabs;
  @FXML
  private Accordion sourcesAccordion;
  @FXML
  private WidgetGallery widgetGallery;
  @FXML
  private Pane handle;
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
    tabs.maxWidthProperty().bind(root.widthProperty().subtract(handle.widthProperty()));
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
    root.widthProperty().addListener((__, old, width) -> {
      expanded.set(width.doubleValue() > handle.getWidth());
    });
    DrawerResizer.attach(root, handle);
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
        BorderPane contentRoot = new BorderPane();
        contentRoot.setPadding(new Insets(0));
        InteractiveSourceTree tree =
            new InteractiveSourceTree(
                sourceType,
                t -> addComponentToActivePane.accept(t),
                t -> createTabForSource.accept(t)
            );

        TextField searchField = new TextField();
        searchField.setPromptText("Search for data sources");
        tree.getFilterableRoot().predicateProperty().bind(EasyBind.monadic(searchField.textProperty()).map(text -> {
          if (text.isEmpty()) {
            return TreeItemPredicate.always();
          } else {
            return (parent, entry) -> StringUtils.containsIgnoreCase(entry.getViewName(), text)
                || StringUtils.containsIgnoreCase(entry.getName(), text)
                || (entry.getValue() != null
                && StringUtils.containsIgnoreCase(StringUtils.deepToString(entry.getValue()), text));
          }
        }));

        contentRoot.setCenter(tree);
        Glyph searchIcon = GlyphFontRegistry.font("FontAwesome").create(FontAwesome.Glyph.SEARCH);
        searchIcon.setAlignment(Pos.CENTER);
        searchIcon.setMaxHeight(Double.POSITIVE_INFINITY);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        Pane footer = new HBox(4, searchIcon, searchField);
        footer.setPadding(new Insets(2));
        contentRoot.setBottom(footer);

        TitledPane titledPane = new TitledPane(sourceType.getName(), contentRoot);
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
    if (expanded.get()) {
      hide();
    } else {
      show();
    }
  }

  public void hide() {
    expanded.set(false);
    animateDrawer();
  }

  public void show() {
    expanded.set(true);
    animateDrawer();
  }

  private void animateDrawer() {
    if (root.getScene() == null) {
      // Not in a scene; don't bother animating
      if (expanded.get()) {
        root.setMaxWidth(412);
      } else {
        root.setMaxWidth(12);
      }
      return;
    }
    Timeline timeline = new Timeline(60);
    double target = getTargetDrawerWidth();
    timeline.getKeyFrames().add(
        new KeyFrame(
            Duration.millis(150),
            new KeyValue(root.minWidthProperty(), target),
            new KeyValue(root.maxWidthProperty(), target)
        ));
    timeline.playFromStart();
  }

  private double getTargetDrawerWidth() {
    if (expanded.get()) {
      return (double) root.getProperties().getOrDefault(EXPANDED_SIZE_KEY, 412.0);
    } else {
      return handle.getWidth();
    }
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
