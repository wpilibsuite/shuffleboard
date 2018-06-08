package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;

import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * The preloader for shuffleboard. This will display the progress of various startup routines until the main window
 * appears.
 */
public class ShuffleboardPreloader extends Preloader {

  private Stage preloaderStage;
  private PreloaderController controller;

  @Override
  public void start(Stage stage) throws Exception {
    preloaderStage = stage;

    Pane preloaderPane = FXMLLoader.load(PreloaderController.class.getResource("Preloader.fxml"));
    controller = FxUtils.getController(preloaderPane);

    Scene scene = new Scene(preloaderPane);
    scene.getStylesheets().setAll("/edu/wpi/first/shuffleboard/api/base.css");

    stage.setScene(scene);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.show();
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification info) {
    if (info instanceof StateNotification) {
      StateNotification notification = (StateNotification) info;
      controller.setStateText(notification.getState());
      controller.setProgress(notification.getProgress());
    }
  }

  @Override
  public void handleStateChangeNotification(StateChangeNotification info) {
    if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
      preloaderStage.close();
    }
  }

  /**
   * A notification for the progress of a state in the preloader.
   */
  public static final class StateNotification implements PreloaderNotification {

    private final String state;
    private final double progress;

    /**
     * Creates a new state notification.
     *
     * @param state    the state
     * @param progress the progress of the state, in the range [0, 1]
     */
    public StateNotification(String state, double progress) {
      this.state = state;
      this.progress = progress;
    }

    /**
     * Gets the state.
     */
    public String getState() {
      return state;
    }

    /**
     * Gets the progress.
     */
    public double getProgress() {
      return progress;
    }

    @Override
    public String toString() {
      return String.format("StateNotification(state='%s', progress=%s)", state, progress);
    }
  }

}
