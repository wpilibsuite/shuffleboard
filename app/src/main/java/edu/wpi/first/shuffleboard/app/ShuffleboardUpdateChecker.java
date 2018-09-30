package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.components.ShuffleboardDialog;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.OsDetector;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;

import com.github.samcarlberg.updatechecker.Repo;
import com.github.samcarlberg.updatechecker.UpdateChecker;
import com.github.samcarlberg.updatechecker.UpdateStatus;
import com.github.zafarkhaja.semver.Version;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

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
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

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
  private static final String currentVersion = Shuffleboard.getSemverVersion().toString();
  private static final ExecutorService downloadService = ThreadUtils.newDaemonScheduledExecutorService();
  private static final Duration timeout = Duration.ofSeconds(15);

  private final UpdateChecker updateChecker = new UpdateChecker(group, artifact, currentVersion);
  private final ScheduledExecutorService scheduledExecutorService = ThreadUtils.newDaemonScheduledExecutorService();

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
    Duration start = Duration.ofNanos(System.nanoTime());
    UpdateStatus status = updateChecker.getStatus();
    Duration end = Duration.ofNanos(System.nanoTime());
    boolean showAlerts = end.minus(start).compareTo(timeout) < 0;
    if (!showAlerts) {
      log.info("Took longer than expected to check the update server, not showing alerts");
    }
    switch (status) {
      case UP_TO_DATE:
        log.info("Shuffleboard is up-to-date");
        if (showAlerts) {
          FxUtils.runOnFxThread(this::showUpToDateDialog);
        }
        break;
      case UNKNOWN:
        log.warning("Could not determine if new versions are available");
        if (showAlerts) {
          Platform.runLater(this::showErrorDialog);
        }
        break;
      case OUTDATED:
        Version newestVersion = updateChecker.getMostRecentVersionSafe().get();
        log.info("A newer version of shuffleboard is available! Current version: "
            + Shuffleboard.getVersion()
            + ", newest version is " + newestVersion);
        promptToInstall(newestVersion, progressNotifier, onComplete);
        break;
      default:
        throw new IllegalStateException("Unknown status: " + status);
    }
  }

  private void showUpToDateDialog() {
    ShuffleboardDialog dialog = ShuffleboardDialog.createForFxml(
        Shuffleboard.class.getResource("UpToDateDialogPane.fxml"));
    dialog.setCloseOnFocusLost(true);
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    dialog.setHeaderText("Up to date");
    Platform.runLater(dialog.getDialogPane()::requestFocus);
    scheduledExecutorService.schedule(() -> Platform.runLater(dialog::closeAndCancel), 5, TimeUnit.SECONDS);
    dialog.showAndWait();
  }

  private void showErrorDialog() {
    ShuffleboardDialog dialog = ShuffleboardDialog.createForFxml(
        Shuffleboard.class.getResource("ErrorDialogPane.fxml"));
    dialog.setCloseOnFocusLost(false);
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    dialog.setHeaderText("No connection");
    Platform.runLater(dialog.getDialogPane()::requestFocus);
    scheduledExecutorService.schedule(() -> Platform.runLater(dialog::closeAndCancel), 10, TimeUnit.SECONDS);
    dialog.showAndWait();
  }

  private void promptToInstall(Version newestVersion,
                               DoubleConsumer progressNotifier,
                               Consumer<Result<Path>> onComplete) {
    FxUtils.runOnFxThread(() -> {
      ShuffleboardDialog dialog = ShuffleboardDialog.createForFxml(
          UpdatePromptController.class.getResource("UpdatePromptPane.fxml"));
      dialog.setCloseOnFocusLost(true);
      dialog.setHeaderText("Update!");
      UpdatePromptController controller = FxUtils.getController(dialog.getDialogPane().getContent());
      controller.setCurrentVersion(Shuffleboard.getSemverVersion());
      controller.setNewestVersion(newestVersion);
      dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());

      ButtonType accept = new ButtonType("Download", ButtonBar.ButtonData.YES);
      ButtonType decline = new ButtonType("No thanks", ButtonBar.ButtonData.NO);
      dialog.getDialogPane().getButtonTypes().setAll(accept, decline);
      dialog.showAndWait().ifPresent(button -> {
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

  public UpdateChecker getUpdateChecker() {
    return updateChecker;
  }

  /**
   * Downloads the newest release.
   *
   * @param newestVersionLocation the URL of the newest version
   * @param newestVersion         the newest version
   * @param progressNotifier      a callback to use to get notifications of the download progress
   * @param onComplete            a callback to call when the download is completed, or fails for any reason
   */
  public static void downloadNewestRelease(URL newestVersionLocation,
                                           Version newestVersion,
                                           DoubleConsumer progressNotifier,
                                           Consumer<Result<Path>> onComplete) {
    downloadService.submit(() -> {
      log.info("Downloading " + newestVersionLocation);
      try {
        File temp = downloadFile(newestVersionLocation, progressNotifier);
        String runningLocation = Shuffleboard.getRunningLocation();
        Path target;
        if (runningLocation.endsWith(".jar")) {
          target = Paths.get(runningLocation);
          ShutdownHooks.addHook(() -> runCopyScript(temp, target));
        } else {
          target = Paths.get("Shuffleboard-" + newestVersion + ".jar");
          Files.copy(temp.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
          try {
            Files.delete(temp.toPath());
          } catch (IOException e) {
            log.log(Level.WARNING, "Could not delete temporary download file: " + temp, e);
          }
        }
        onComplete.accept(Result.success(target));
      } catch (IOException e) {
        log.log(Level.WARNING, "Could not download release from " + newestVersionLocation, e);
        onComplete.accept(Result.failure(e));
      }
    });
  }

  /**
   * Downloads the remote file denoted by the given URL.
   *
   * @param remoteFile       the location of the remote file
   * @param progressNotifier a callback to get notifications of the download progress
   *
   * @return the downloaded file
   *
   * @throws IOException if the file could not be downloaded due to an I/O error
   */
  private static File downloadFile(URL remoteFile, DoubleConsumer progressNotifier) throws IOException {
    File temp = File.createTempFile("newestshuffleboard", ".jar");
    log.finer("Downloading to " + temp);
    URLConnection connection = remoteFile.openConnection();
    connection.setReadTimeout(5000);    // ms
    connection.setConnectTimeout(5000); // ms
    try (OutputStream out = new FileOutputStream(temp)) {
      try (InputStream in = connection.getInputStream()) {
        final long fileSize = getFileSize(remoteFile);
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
      } finally {
        out.close();
      }
      return temp;
    }
  }

  /**
   * Gets the size of a file denoted by a URL. If the URL points to an HTTP resource, the file size will be queried;
   * otherwise, this method will return {@code 0}.
   *
   * @param url the URL denoting the file to get the size of
   *
   * @return the size of the remote file
   *
   * @throws IOException if the URL could not be connected to
   */
  private static long getFileSize(URL url) throws IOException {
    long fileSize = 0;
    URLConnection connection = url.openConnection();
    if (connection instanceof HttpURLConnection) {
      ((HttpURLConnection) connection).setRequestMethod("HEAD");
      connection.getInputStream(); // required to get the content header
      fileSize = connection.getContentLengthLong();
      log.info("File size: " + fileSize + " bytes");
    }
    return fileSize;
  }

  /**
   * Runs the copy and restart script.
   *
   * @param temp   the temp file that the update was downloaded to
   * @param target the path to copy the downloaded file to
   *
   * @throws InterruptedException if the calling thread is interrupted while the script is running
   */
  @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE") // Complains about Files.copy(in, scriptFile) ...
  private static void runCopyScript(File temp, Path target) throws InterruptedException {
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
      // Copy the jar to the backups folder
      Files.copy(
          target,
          Paths.get(Storage.getBackupsDir().toString(), "Shuffleboard-" + Shuffleboard.getVersion() + ".jar"),
          StandardCopyOption.REPLACE_EXISTING);
      Path scriptFile = Files.createTempFile("copy_and_restart", scriptFileExtension);
      try (InputStream in = Shuffleboard.class.getResourceAsStream("/copy_and_restart" + scriptFileExtension)) {
        Files.copy(in, scriptFile, StandardCopyOption.REPLACE_EXISTING);
        if (OsDetector.getOperatingSystemType() == MAC || OsDetector.getOperatingSystemType() == LINUX) {
          // need to make the extracted script executable on Mac and Linux
          makeExecutable(scriptFile);
        }
        // The scripts take the temp file as the first argument and the destination as the second
        ProcessBuilder pb = new ProcessBuilder(
            scriptFile.toString(), temp.getAbsolutePath(), target.toString());
        pb.inheritIO();
        pb.start();
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not copy newest jar!", e);
    }
  }

  /**
   * Makes a file executable if called on a Unix-based system (Mac, Linux). This will block until the {@code chmod}
   * command completes or until the thread is interrupted.
   *
   * @param path the path to the file to make executable
   *
   * @throws InterruptedException if the thread was interrupted before {@code chmod} could complete
   * @throws IOException          if an I/O error occurs while running the command
   */
  private static void makeExecutable(Path path) throws InterruptedException, IOException {
    new ProcessBuilder("chmod", "u+x", path.toString())
        .inheritIO()
        .start()
        .waitFor();
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
