package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.OsDetector;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import static edu.wpi.first.shuffleboard.api.util.OsDetector.OperatingSystemType.LINUX;
import static edu.wpi.first.shuffleboard.api.util.OsDetector.OperatingSystemType.MAC;

/**
 * Allows an easy way to check for updates and prompt the user to install the newest available version, if applicable.
 */
public final class ShuffleboardUpdateChecker {

  private static final Logger log = Logger.getLogger(ShuffleboardUpdateChecker.class.getName());

  private static final String group = "edu.wpi.first.shuffleboard";
  private static final String artifact = "app";
  private static final String releaseRepo = "http://first.wpi.edu/FRC/roborio/maven/release/";
  private static final String version;
  private static final ExecutorService downloadService = ThreadUtils.newDaemonScheduledExecutorService();

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
    updateChecker.usingRepos(Repo.maven("FRC Maven Release Server", createUrlUnchecked(releaseRepo)));
  }

  private static URL createUrlUnchecked(String iSwearThisIsSafe) {
    try {
      return new URL(iSwearThisIsSafe);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("The URL string is malformed! " + iSwearThisIsSafe, e);
    }
  }

  /**
   * Checks for updates to shuffleboard, and prompts the user to update if a newer version is available.
   *
   * @param progressNotifier a callback to be called to be notified of the download progress. The progress is reported
   *                         as a double in the range (0, 1), and may not update at regular intervals.
   * @param onComplete       a callback to be called after the download completes or fails
   */
  public void checkForUpdatesAndPromptToInstall(DoubleConsumer progressNotifier, Consumer<Result<Path>> onComplete) {
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
        promptToInstall(Version.valueOf(version), newestVersion, progressNotifier, onComplete);
        break;
      default:
        throw new IllegalStateException("Unknown status: " + status);
    }
  }

  private void promptToInstall(Version currentVersion,
                               Version newestVersion,
                               DoubleConsumer progressNotifier,
                               Consumer<Result<Path>> onComplete) {
    FxUtils.runOnFxThread(() -> {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
      alert.setTitle("An update is available");
      alert.setHeaderText("A newer version of shuffleboard is available for download!");
      StringBuilder messageBuilder = new StringBuilder();
      messageBuilder.append("  ").append("Current version: ").append(currentVersion).append('\n');
      messageBuilder.append("  ").append("Newest version: ").append(newestVersion).append('\n');
      if (newestVersion.getMajorVersion() > currentVersion.getMajorVersion()) {
        // Major version bump, may cause custom plugins to break. Alert the user
        messageBuilder.append('\n').append("  ").append("WARNING: There has been a major version bump! " +
            "Custom plugins may no longer load or behave properly");
      }
      Label content = new Label(messageBuilder.toString());
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
              downloadNewestRelease(location, newestVersion, progressNotifier, onComplete);
            });
          } catch (IOException e) {
            log.log(Level.WARNING, "Could not determine newest version", e);
            onComplete.accept(Result.failure(e));
          }
        }
      });
    });
  }

  private static void downloadNewestRelease(URL newestVersionLocation,
                                            Version newestVersion,
                                            DoubleConsumer progressNotifier,
                                            Consumer<Result<Path>> onComplete) {
    downloadService.submit(() -> {
      log.info("Downloading " + newestVersionLocation);
      try {
        File temp = File.createTempFile("newestshuffleboard", ".jar");
        log.finer("Downloading to " + temp);
        try (OutputStream out = new FileOutputStream(temp)) {
          try (InputStream in = newestVersionLocation.openStream()) {
            long fileSize = 0;
            URLConnection connection = newestVersionLocation.openConnection();
            if (connection instanceof HttpURLConnection) {
              ((HttpURLConnection) connection).setRequestMethod("HEAD");
              connection.getInputStream(); // required to get the content header
              fileSize = connection.getContentLengthLong();
              log.info("File size: " + fileSize + " bytes");
            }
            long totalRead = 0;
            byte[] buf = new byte[4096];
            int read;
            while ((read = in.read(buf)) != -1) {
              out.write(buf, 0, read);
              totalRead += read;
              if (fileSize > 0) {
                progressNotifier.accept((double) totalRead / fileSize);
              }
            }
            progressNotifier.accept(1.0);
            log.info("Done downloading");
          }
        }
        // 6 -> "file:/".length() to change file:/C:/... to C:/...
        // 5 -> "file:".length() to change file:/home/... to /home/...
        int prefixLen = OsDetector.getOperatingSystemType() == OsDetector.OperatingSystemType.WINDOWS ? 6 : 5;
        String runningLocation = Shuffleboard.getRunningLocation().substring(prefixLen);
        Path target;
        if (runningLocation.endsWith(".jar")) {
          target = Paths.get(runningLocation);
          ShutdownHooks.addHook(() -> {
            try {
              final String scriptFileExtension;
              switch (OsDetector.getOperatingSystemType()) {
                case WINDOWS:
                  scriptFileExtension = ".bat";
                  break;
                case MAC:
                  scriptFileExtension = "-mac.sh";
                  break;
                case LINUX:
                  scriptFileExtension = "-linux.sh";
                  break;
                default:
                  throw new AssertionError("Unknown OS type " + OsDetector.getOperatingSystemType());
              }
              Path scriptFile = Files.createTempFile("copy_and_restart", scriptFileExtension);
              try (InputStream in = Shuffleboard.class.getResourceAsStream("/copy_and_restart" + scriptFileExtension)) {
                Files.copy(in, scriptFile, StandardCopyOption.REPLACE_EXISTING);
                if (OsDetector.getOperatingSystemType() == MAC || OsDetector.getOperatingSystemType() == LINUX) {
                  // need to make the extracted script executable
                  new ProcessBuilder("chmod", "u+x", scriptFile.toString())
                      .inheritIO()
                      .start()
                      .waitFor();
                }
                ProcessBuilder pb = new ProcessBuilder(
                    scriptFile.toString(), temp.getAbsolutePath(), target.toString());
                pb.inheritIO();
                pb.start();
              }
            } catch (IOException e) {
              log.log(Level.WARNING, "Could not copy newest jar!", e);
            }
          });
        } else {
          target = Paths.get("Shuffleboard-" + newestVersion + ".jar");
          Files.copy(temp.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
          temp.delete();
        }
        onComplete.accept(Result.success(target));
      } catch (IOException e) {
        log.log(Level.WARNING, "Could not download release from " + newestVersionLocation, e);
      }
    });
  }

  public static final class Result<T> {

    private final Throwable error;
    private final T value;

    public static <T> Result<T> success(T value) {
      return new Result<>(null, value);
    }

    public static <T> Result<T> failure(Throwable error) {
      return new Result<>(error, null);
    }

    private Result(Throwable error, T value) {
      this.error = error;
      this.value = value;
    }

    public boolean failed() {
      return error != null;
    }

    public boolean succeeded() {
      return error == null;
    }

    public Throwable getError() {
      return error;
    }

    public T getValue() {
      return value;
    }
  }

}
