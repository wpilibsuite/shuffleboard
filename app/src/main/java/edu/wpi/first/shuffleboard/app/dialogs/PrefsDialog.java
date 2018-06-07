package edu.wpi.first.shuffleboard.app.dialogs;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet.PropertyItem;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import java.util.function.Function;
import java.util.function.Predicate;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.StageStyle;

/**
 * Dialog for editing application and plugin preferences.
 */
public final class PrefsDialog {

  private static final String DIALOG_TITLE = "Shuffleboard Preferences";

  private final TabPane tabs = new TabPane();
  private final Predicate<Plugin> hasProperties = plugin -> !plugin.getProperties().isEmpty();
  private final Function<Plugin, Tab> createTabForPlugin = this::createTabForPlugin;

  public PrefsDialog() {
    tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
  }

  /**
   * Shows the preferences dialog.
   */
  public void show() {
    tabs.getTabs().clear();
    tabs.getTabs().add(new Tab("Application", new ExtendedPropertySheet(AppPreferences.getInstance().getProperties())));

    PluginLoader.getDefault().getLoadedPlugins().stream()
        .filter(hasProperties)
        .map(createTabForPlugin)
        .forEach(tabs.getTabs()::add);

    Dialog<Boolean> dialog = createDialog();
    if (dialog.showAndWait().orElse(false)) {
      tabs.getTabs().stream()
          .map(t -> (ExtendedPropertySheet) t.getContent())
          .flatMap(p -> p.getItems().stream())
          .flatMap(TypeUtils.castStream(PropertyItem.class))
          .map(i -> (PropertyItem<?>) i) // due to castStream not working on generic types
          .map(PropertyItem::getObservableValue)
          .flatMap(TypeUtils.optionalStream())
          .flatMap(TypeUtils.castStream(FlushableProperty.class))
          .filter(FlushableProperty::isChanged)
          .forEach(FlushableProperty::flush);
    }
  }

  private Dialog<Boolean> createDialog() {
    Dialog<Boolean> dialog = new Dialog<>();
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    dialog.getDialogPane().setContent(new BorderPane(tabs));
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.initStyle(StageStyle.UTILITY);
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    dialog.setTitle(DIALOG_TITLE);
    dialog.setResizable(true);
    dialog.setResultConverter(button -> !button.getButtonData().isCancelButton());
    return dialog;
  }

  private Tab createTabForPlugin(Plugin plugin) {
    Tab tab = new Tab(plugin.getName());
    tab.setContent(new ExtendedPropertySheet(plugin.getProperties()));
    tab.setDisable(DashboardMode.getCurrentMode() == DashboardMode.PLAYBACK);
    return tab;
  }

}
