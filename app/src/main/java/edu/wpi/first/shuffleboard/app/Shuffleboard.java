package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.recording.Converters;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.Time;
import edu.wpi.first.shuffleboard.app.plugin.PluginCache;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.sources.recording.CsvConverter;
import edu.wpi.first.shuffleboard.plugin.base.BasePlugin;
import edu.wpi.first.shuffleboard.plugin.cameraserver.CameraServerPlugin;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;

import com.google.common.base.Stopwatch;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

@SuppressWarnings("PMD.MoreThanOneLogger") // there's only one logger used, the others are for setting up file logging
public class Shuffleboard extends Application {

  private static final Logger logger = Logger.getLogger(Shuffleboard.class.getName());

  private Pane mainPane; //NOPMD local variable
  private Runnable onOtherAppStart = () -> {};

  private final Stopwatch startupTimer = Stopwatch.createStarted();
  private MainWindowController mainWindowController;

  @Override
  public void init() throws AlreadyLockedException, IOException, InterruptedException {
    try {
      JUnique.acquireLock(getClass().getCanonicalName(), message -> {
        onOtherAppStart.run();
        return null;
      });
    } catch (AlreadyLockedException alreadyLockedException) {
      JUnique.sendMessage(getClass().getCanonicalName(), "alreadyRunning");
      throw alreadyLockedException;
    }

    Loggers.setupLoggers();

    // Search for and load themes from the custom theme directory before loading application preferences
    // This avoids an issue with attempting to load a theme at startup that hasn't yet been registered
    logger.finer("Registering custom user themes from external dir");
    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading custom themes", 0));
    Themes.getDefault().loadThemesFromDir();

    logger.info("Build time: " + getBuildTime());

    // Before we load components that only work with Java 8, check to make sure
    // the application is running on Java 8. If we are running on an invalid
    // version, show an alert and exit before we get into trouble.

    Converters.getDefault().register(CsvConverter.Instance);

    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading base plugin", 0.125));
    PluginLoader.getDefault().load(new BasePlugin());

    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading NetworkTables plugin", 0.25));
    PluginLoader.getDefault().load(new NetworkTablesPlugin());
    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading CameraServer plugin", 0.375));
    PluginLoader.getDefault().load(new CameraServerPlugin());
    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading custom plugins", 0.625));
    PluginLoader.getDefault().loadAllJarsFromDir(Storage.getPluginPath());
    notifyPreloader(new ShuffleboardPreloader.StateNotification("Loading custom plugins", 0.75));
    PluginCache.getDefault().loadCache(PluginLoader.getDefault());
    Stopwatch fxmlLoadTimer = Stopwatch.createStarted();

    notifyPreloader(new ShuffleboardPreloader.StateNotification("Initializing user interface", 0.875));
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
    mainPane = loader.load();
    long fxmlLoadTime = fxmlLoadTimer.elapsed(TimeUnit.MILLISECONDS);
    mainWindowController = loader.getController();
    logger.log(fxmlLoadTime >= 500 ? Level.WARNING : Level.INFO, "Took " + fxmlLoadTime + "ms to load the main FXML");

    notifyPreloader(new ShuffleboardPreloader.StateNotification("Starting up", 1));
    Thread.sleep(20); // small wait to let the status be visible - the preloader doesn't get notifications for a bit
  }

  @Override
  public void start(Stage primaryStage) {
    // Set up the application thread to log exceptions instead of using printStackTrace()
    // Must be called in start() because init() is run on the main thread, not the FX application thread
    Thread.currentThread().setUncaughtExceptionHandler(Shuffleboard::uncaughtException);
    onOtherAppStart = () -> Platform.runLater(primaryStage::toFront);

    primaryStage.setScene(new Scene(mainPane));

    // Setup the dashboard tabs after all plugins are loaded
    Platform.runLater(() -> {
      File lastSaveFile = AppPreferences.getInstance().getSaveFile();
      if (AppPreferences.getInstance().isAutoLoadLastSaveFile() && lastSaveFile != null && lastSaveFile.exists()) {
        try {
          mainWindowController.load(lastSaveFile);
        } catch (RuntimeException | IOException e) {
          logger.log(Level.WARNING, "Could not load the last save file", e);
          mainWindowController.newLayout();
        }
      } else {
        mainWindowController.newLayout();
      }
    });

    primaryStage.setTitle("Shuffleboard");
    primaryStage.setMinWidth(640);
    primaryStage.setMinHeight(480);
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.setOnCloseRequest(closeEvent -> {
      if (!AppPreferences.getInstance().isConfirmExit()) {
        // Don't show the confirmation dialog, just exit
        return;
      }
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Save layout");
      alert.getDialogPane().getScene().getStylesheets().setAll(mainPane.getStylesheets());
      alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
      alert.getDialogPane().setHeaderText("Save the current layout before closing?");
      alert.showAndWait().ifPresent(bt -> {
        if (bt == ButtonType.YES) {
          try {
            mainWindowController.save();
          } catch (IOException ex) {
            logger.log(Level.WARNING, "Could not save the layout", ex);
          }
        } else if (bt == ButtonType.CANCEL) {
          // cancel the close request by consuming the event
          closeEvent.consume();
        }
        // Don't need to check for NO because it just lets the window close normally
      });
    });

    primaryStage.show();
    Time.setStartTime(Time.now());
    long startupTime = startupTimer.elapsed(TimeUnit.MILLISECONDS);
    logger.log(startupTime > 5000 ? Level.WARNING : Level.INFO, "Took " + startupTime + "ms to start Shuffleboard");
  }

  @Override
  public void stop() throws Exception {
    logger.info("Running shutdown hooks");
    ShutdownHooks.runAllHooks();
    logger.info("Shutting down");
  }

  /**
   * Logs an uncaught exception on a thread. This is in a method instead of directly in a lambda to make the log a bit
   * cleaner ({@code edu.wpi.first.shuffleboard.app.Shuffleboard uncaughtException} vs
   * {@code edu.wpi.first.shuffleboard.app.Shuffleboard start$lambda$2$}).
   *
   * @param thread    the thread on which the exception was thrown
   * @param throwable the uncaught exception
   */
  private static void uncaughtException(Thread thread, Throwable throwable) {
    logger.log(Level.WARNING, "Uncaught exception on " + thread.getName(), throwable);
  }

  /**
   * Gets the time at which the application JAR was built, or the instant this was first called if shuffleboard is not
   * running from a JAR.
   */
  public static Instant getBuildTime() {
    return ApplicationManifest.getBuildTime();
  }

  /**
   * Gets the current shuffleboard version.
   */
  public static String getVersion() {
    // Try to get the version from the shuffleboard class. This will return null when running from source (eg using
    // gradle run or similar), so in that case we fall back to getting the version from an API class, which will always
    // have its version set in that case
    String appVersion = Shuffleboard.class.getPackage().getImplementationVersion();
    if (appVersion != null) {
      return appVersion;
    }
    return Storage.class.getPackage().getImplementationVersion();
  }

  /**
   * Gets the location from which shuffleboard is running. If running from a JAR, this will be the location of the JAR;
   * otherwise, it will likely be the root build directory of the `app` project.
   */
  public static String getRunningLocation() {
    try {
      return new File(Shuffleboard.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
    } catch (URISyntaxException e) {
      throw new AssertionError("Local file URL somehow had invalid syntax!", e);
    }
  }

}
