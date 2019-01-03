package edu.wpi.first.shuffleboard.app;

import com.sun.javafx.application.LauncherImpl;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The true main class.  This bypasses module boot layer introspection by the Java launcher that attempts to
 * reflectively access the JavaFX application launcher classes - this will fail because there is no module path;
 * everything is in the same, unnamed module.  This is also how we are able to call {@link LauncherImpl} directly,
 * which is not in a package exported by the {@code javafx.graphics} module.
 */
@SuppressWarnings("PMD.UseUtilityClass") // Nope.
public final class Main {
  private static final Logger logger = Logger.getLogger(Main.class.getName());

  @SuppressWarnings("JavadocMethod")
  public static void main(String[] args) {
    // JavaFX 11+ uses GTK3 by default, and has problems on some display servers
    // This flag forces JavaFX to use GTK2
    System.setProperty("jdk.gtk.version", "2");

    Thread
        .setDefaultUncaughtExceptionHandler((thread, throwable) -> {
          logger.log(
              Level.SEVERE,
              "Uncaught exception on thread `" + thread.getName() + "`.",
              throwable
          );
        });
    LauncherImpl.launchApplication(Shuffleboard.class, ShuffleboardPreloader.class, args);
  }
}
