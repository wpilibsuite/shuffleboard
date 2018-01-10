package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.github.samcarlberg.updatechecker.Repo;
import com.github.samcarlberg.updatechecker.UpdateChecker;
import com.github.samcarlberg.updatechecker.UpdateStatus;
import com.github.zafarkhaja.semver.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

/**
 * Allows an easy way to check for updates and prompt the user to install the newest available version, if applicable.
 */
public final class ShuffleboardUpdateChecker {

  private static final Logger log = Logger.getLogger(ShuffleboardUpdateChecker.class.getName());

  private static final String group = "edu.wpi.first.shuffleboard";
  private static final String artifact = "app";
  private static final String releaseRepo = "http://first.wpi.edu/FRC/roborio/maven/release/";
  private static final String version;

  static {
    // Remove leading letters like "v" or "version" from the string
    String sbVersion = Shuffleboard.getVersion();
    int index = 0;
    while (Character.isAlphabetic(sbVersion.charAt(index))) {
      index++;
    }
    version = sbVersion.substring(index);
  }

  private final UpdateChecker updateChecker = new UpdateChecker(group, artifact, version);

  public ShuffleboardUpdateChecker() {
    updateChecker.usingRepos(Repo.maven("FRC Maven Release Server", createUrlYouDummy(releaseRepo)));
  }

  static URL createUrlYouDummy(String iSwearThisIsSafe) {
    try {
      return new URL(iSwearThisIsSafe);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("You were wrong! Malformed URL: " + iSwearThisIsSafe, e);
    }
  }

  /**
   * Checks for updates to shuffleboard, and prompts the user to update if a newer version is available.
   */
  public void checkForUpdatesAndPromptToInstall() {
    UpdateStatus status = updateChecker.getStatus();
    switch (status) {
      case UP_TO_DATE:
        log.info("Shuffleboard is up-to-date");
        break;
      case UNKNOWN:
        log.warning("Could not determine if new versions are available");
        break;
      case OUTDATED:
        Version newestVersion = updateChecker.getMostRecentVersionSafe().get();
        log.info("A newer version of shuffleboard is available! Current version: "
            + Shuffleboard.getVersion()
            + ", newest version is " + newestVersion);
        promptToInstall(Version.valueOf(version), newestVersion);
        break;
      default:
        throw new IllegalStateException("Unknown status: " + status);
    }
  }

  private void promptToInstall(Version currentVersion, Version newestVersion) {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
      alert.setTitle("An update is available");
      alert.setHeaderText("A newer version of shuffleboard is available for download!");
      Label content = new Label("  Current version: " + currentVersion + "\n  Newest version: " + newestVersion);
      content.setPadding(new Insets(8));
      alert.getDialogPane().setContent(content);
      ButtonType accept = new ButtonType("Download", ButtonBar.ButtonData.YES);
      ButtonType decline = new ButtonType("No thanks", ButtonBar.ButtonData.NO);
      alert.getButtonTypes().setAll(accept, decline);
      Platform.runLater(alert.getDialogPane()::requestFocus);
      alert.showAndWait().ifPresent(button -> {
        if (button.equals(accept)) {
          try {
            updateChecker.getMostRecentArtifactLocation().ifPresent(location -> {
              downloadNewestRelease(location, newestVersion);
            });
          } catch (IOException e) {
            log.log(Level.WARNING, "Could not determine newest version", e);
          }
        }
      });
    });
  }

  private static void downloadNewestRelease(URL newestVersionLocation, Version newestVersion) {
    if (Files.exists(Paths.get("Shuffleboard-" + newestVersion + ".jar"))) {
      log.info("Newest version is already present");
      return;
    }
    Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread).submit(() -> {
      log.info("Downloading " + newestVersionLocation);
      try {
        File temp = File.createTempFile("newestshuffleboard", ".jar");
        log.finer("Downloading to " + temp);
        try (OutputStream out = new FileOutputStream(temp)) {
          try (InputStream in = newestVersionLocation.openStream()) {
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
              out.write(buf, 0, read);
            }
            log.info("Done downloading");
          }
        }
        log.finer("Copying to local dir");
        Files.copy(temp.toPath(), Paths.get("Shuffleboard-" + newestVersion + ".jar"));
        temp.delete();
      } catch (IOException e) {
        log.log(Level.WARNING, "Could not download release from " + newestVersionLocation, e);
      }
    });
  }

}
