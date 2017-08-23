package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.plugin.base.BasePlugin;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Shuffleboard extends Application {

  private Pane mainPane; //NOPMD local variable
  private Runnable onOtherAppStart = () -> {};

  @Override
  public void init() throws AlreadyLockedException, IOException {
    try {
      JUnique.acquireLock(getClass().getCanonicalName(), message -> {
        onOtherAppStart.run();
        return null;
      });
    } catch (AlreadyLockedException alreadyLockedException) {
      JUnique.sendMessage(getClass().getCanonicalName(), "alreadyRunning");
      throw alreadyLockedException;
    }

    NetworkTablesJNI.startClient("localhost", 1735);

    // Load the Roboto font
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Regular.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Bold.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Italic.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-BoldItalic.ttf").openStream(), -1);

    // Install SVG image loaders so SVGs can be used like any other image
    SvgImageLoaderFactory.install();
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
    onOtherAppStart = () -> Platform.runLater(primaryStage::toFront);
    primaryStage.setScene(new Scene(mainPane));

    PluginLoader.getDefault().load(new BasePlugin());
    PluginLoader.getDefault().load(new NetworkTablesPlugin());
    loadPluginsFromDir();

    Recorder.getInstance().start();
    primaryStage.setTitle("Shuffleboard");
    primaryStage.setMinWidth(640);
    primaryStage.setMinHeight(480);
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.show();
  }

  /**
   * Attempts to loads plugins from all jars found in in the {@link Storage#PLUGINS_DIR plugin directory}. This will
   * overwrite pre-existing plugins with the same ID string (eg "edu.wpi.first.shuffleboard.Base") in encounter order,
   * which is alphabetical by jar name. For example, if a jar file "my_plugins.jar" defines a plugin with ID "foo.bar"
   * and another jar file "more_plugins.jar" <i>also</i> defines a plugin with that ID, the plugin from "more_plugins"
   * will be loaded first, then unloaded and replaced with the one from "my_plugins.jar". For this reason, plugin
   * authors should be careful to use unique group IDs. We recommend Java's reverse-DNS naming scheme.
   *
   * @throws IOException if the plugin directory could not be read
   */
  private void loadPluginsFromDir() throws IOException {
    Path pluginPath = Paths.get(Storage.PLUGINS_DIR);
    if (!Files.exists(pluginPath)) {
      Files.createDirectories(pluginPath);
    }
    PluginLoader.getDefault().loadAllJarsFromDir(pluginPath);
  }

}
