package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.Time;
import edu.wpi.first.shuffleboard.app.plugin.PluginCache;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.plugin.base.BasePlugin;
import edu.wpi.first.shuffleboard.plugin.cameraserver.CameraServerPlugin;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;
import edu.wpi.first.shuffleboard.plugin.powerup.PowerupPlugin;

import com.github.zafarkhaja.semver.Version;

import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import java.io.IOException;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

@SuppressWarnings("PMD.MoreThanOneLogger") // there's only one logger used, the others are for setting up file logging
public class Shuffleboard extends Application {

  private static final Logger logger = Logger.getLogger(Shuffleboard.class.getName());

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

    // Set up the loggers
    setupLoggers();

    // Install SVG image loaders so SVGs can be used like any other image
    SvgImageLoaderFactory.install();

    // Search for and load themes from the custom theme directory before loading application preferences
    // This avoids an issue with attempting to load a theme at startup that hasn't yet been registered
    logger.finer("Registering custom user themes from external dir");
    Themes.getDefault().loadThemesFromDir();

    logger.info("Build time: " + getBuildTime());
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    // Set up the application thread to log exceptions instead of using printStackTrace()
    // Must be called in start() because init() is run on the main thread, not the FX application thread
    Thread.currentThread().setUncaughtExceptionHandler(Shuffleboard::uncaughtException);
    onOtherAppStart = () -> Platform.runLater(primaryStage::toFront);

    // Before we load components that only work with Java 8, check to make sure
    // the application is running on Java 8. If we are running on an invalid
    // version, show an alert and exit before we get into trouble.
    String javaSpec = System.getProperty("java.specification.version");
    String javaVersion = System.getProperty("java.version");
    if (!"1.8".equals(javaSpec)) {
      Alert invalidVersionAlert = new Alert(Alert.AlertType.ERROR);
      invalidVersionAlert.setHeaderText("Invalid JRE Version!");
      invalidVersionAlert.setContentText(
          String.format("You are using an unsupported Java version: %s%n"
                  + "Please download Java 8 and uninstall Java %s.",
              javaVersion, javaSpec));
      invalidVersionAlert.initStyle(StageStyle.UNDECORATED);
      invalidVersionAlert.getDialogPane().getStylesheets().setAll(
          AppPreferences.getInstance().getTheme().getStyleSheets());
      invalidVersionAlert.showAndWait();

      return;
    }

    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
    mainPane = loader.load();
    final MainWindowController mainWindowController = loader.getController();

    primaryStage.setScene(new Scene(mainPane));

    PluginLoader.getDefault().load(new BasePlugin());

    Recorder.getInstance().start();

    PluginLoader.getDefault().load(new NetworkTablesPlugin());
    PluginLoader.getDefault().load(new CameraServerPlugin());
    PluginLoader.getDefault().load(new PowerupPlugin());
    PluginLoader.getDefault().loadAllJarsFromDir(Storage.getPluginPath());
    PluginCache.getDefault().loadCache(PluginLoader.getDefault());

    // Setup the dashboard tabs after all plugins are loaded
    Platform.runLater(() -> {
      if (AppPreferences.getInstance().isAutoLoadLastSaveFile()) {
        try {
          mainWindowController.load(AppPreferences.getInstance().getSaveFile());
        } catch (IOException e) {
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

    if (AppPreferences.getInstance().isCheckForUpdatesOnStartup()) {
      mainWindowController.checkForUpdatesSubdued();
    }
    primaryStage.show();
    Time.setStartTime(Time.now());
  }

  @Override
  public void stop() throws Exception {
    logger.info("Running shutdown hooks");
    ShutdownHooks.runAllHooks();
    logger.info("Shutting down");
  }

  /**
   * Sets up loggers to print to stdout (rather than stderr) and log to ~/Shuffleboard/shuffleboard.log
   */
  private void setupLoggers() throws IOException {
    //Set up the global level logger. This handles IO for all loggers.
    final Logger globalLogger = LogManager.getLogManager().getLogger("");

    // Remove the default handlers that stream to System.err
    for (Handler handler : globalLogger.getHandlers()) {
      globalLogger.removeHandler(handler);
    }

    final Handler fileHandler = new FileHandler(Storage.getStorageDir() + "/shuffleboard.log");

    fileHandler.setLevel(Level.INFO);    // Only log INFO and above to disk
    globalLogger.setLevel(Level.CONFIG); // Log CONFIG and higher

    // We need to stream to System.out instead of System.err
    final StreamHandler sh = new StreamHandler(System.out, new SimpleFormatter()) {
      @Override
      public synchronized void publish(final LogRecord record) { // NOPMD this is the same signature as the superclass
        super.publish(record);
        // For some reason this doesn't happen automatically.
        // This will ensure we get all of the logs printed to the console immediately instead of at shutdown
        flush();
      }
    };
    sh.setLevel(Level.CONFIG); // Log CONFIG and higher to stdout

    globalLogger.addHandler(sh);
    globalLogger.addHandler(fileHandler);
    fileHandler.setFormatter(new SimpleFormatter()); //log in text, not xml

    globalLogger.config("Configuration done."); //Log that we are done setting up the logger
    globalLogger.config("Shuffleboard app version: " + getVersion());
    globalLogger.config("Running from " + getRunningLocation());
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
   * Gets a Version object representing the current shuffleboard version.
   */
  public static Version getSemverVersion() {
    String rawVersion = getVersion();
    for (int index = 0; index < rawVersion.length(); index++) {
      if (Character.isDigit(rawVersion.charAt(index))) {
        return Version.valueOf(rawVersion.substring(index));
      }
    }
    throw new IllegalStateException("Invalid semver string: " + rawVersion
        + ". Please open an issue on Github or contact a developer");
  }

  /**
   * Gets the location from which shuffleboard is running. If running from a JAR, this will be the location of the JAR;
   * otherwise, it will likely be the root build directory of the `app` project.
   */
  public static String getRunningLocation() {
    return Shuffleboard.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
  }

}
