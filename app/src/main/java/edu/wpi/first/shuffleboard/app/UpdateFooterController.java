package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.DoubleConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Pane;

/**
 * Controller for the update footer. The root should default to be unmanaged.
 */
public final class UpdateFooterController {

  private static final Logger log = Logger.getLogger(UpdateFooterController.class.getName());

  @FXML
  private Pane root;
  @FXML
  private Label text;
  @FXML
  private Pane downloadArea;
  @FXML
  private ProgressBar downloadBar;
  @FXML
  private Button downloadButton;
  @FXML
  private Button cancelButton;

  private final ExecutorService updateService = Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread);

  private ShuffleboardUpdateChecker shuffleboardUpdateChecker;

  @FXML
  private void initialize() {
    FxUtils.setController(root, this);
  }

  /**
   * Checks for updates to shuffleboard. If an update is available, makes the root managed and visible and prompts the
   * user to download the update.
   */
  public void checkForUpdatesAndPrompt() {
    updateService.submit(() -> {
      switch (shuffleboardUpdateChecker.getUpdateChecker().getStatus()) {
        case OUTDATED:
          root.setManaged(true);
          break;
        case UP_TO_DATE:
          log.info("Shuffleboard is up-to-date");
          break;
        default:
          // Unknown status; the internal UpdateChecker wil have logged the error
          break;
      }
    });
  }

  @FXML
  private void downloadUpdate() throws IOException {
    shuffleboardUpdateChecker.getUpdateChecker().getMostRecentArtifactLocation().ifPresent(url -> {
      text.setManaged(false);
      downloadButton.setManaged(false);
      downloadArea.setManaged(true);
      DoubleConsumer progressNotifier = progress -> FxUtils.runOnFxThread(() -> downloadBar.setProgress(progress));
      ShuffleboardUpdateChecker.downloadNewestRelease(url,Shuffleboard.getSemverVersion(), progressNotifier, result -> {
        if (result.succeeded()) {
          Platform.exit(); // This will trigger the copy
        } else if (result.failed()) {
          log.log(Level.WARNING, "Could not download update", result.getError());
          root.setManaged(false);
        }
      });
    });
  }

  @FXML
  private void cancelUpdate() {
    root.setManaged(false);
  }

  public void setShuffleboardUpdateChecker(ShuffleboardUpdateChecker shuffleboardUpdateChecker) {
    this.shuffleboardUpdateChecker = shuffleboardUpdateChecker;
  }
}
