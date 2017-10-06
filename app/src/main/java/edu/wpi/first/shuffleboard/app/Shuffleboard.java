package edu.wpi.first.shuffleboard.app;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.plugin.base.BasePlugin;
import edu.wpi.first.shuffleboard.plugin.cameraserver.CameraServerPlugin;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
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

    ChangeListener<String> serverChangeListener = (observable, oldValue, newValue) -> {
      NetworkTable.shutdown();

      String[] value = newValue.split(":");

      /*
       * You MUST set the port number before setting the team number.
       */
      if (value.length > 1) {
        NetworkTable.setPort(Integer.parseInt(value[1]));
      } else {
        NetworkTable.setPort(NetworkTable.DEFAULT_PORT);
      }

      if (value[0].matches("\\d{1,4}")) {
        NetworkTable.setTeam(Integer.parseInt(value[0]));
      } else if (value[0].isEmpty()) {
        NetworkTable.setIPAddress("localhost");
      } else {
        NetworkTable.setIPAddress(value[0]);
      }

      NetworkTable.initialize();
    };
    NetworkTable.setClientMode();
    serverChangeListener.changed(null, null, AppPreferences.getInstance().getServer());
    AppPreferences.getInstance().serverProperty().addListener(serverChangeListener);

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
    PluginLoader.getDefault().load(new CameraServerPlugin());
    PluginLoader.getDefault().load(new NetworkTablesPlugin());
    PluginLoader.getDefault().loadAllJarsFromDir(Storage.getPluginPath());

    Recorder.getInstance().start();
    primaryStage.setTitle("Shuffleboard");
    primaryStage.setMinWidth(640);
    primaryStage.setMinHeight(480);
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.show();
  }
}
