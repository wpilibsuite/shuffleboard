package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.controller.ShuffleboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static com.google.common.base.Preconditions.checkNotNull;

public class Shuffleboard extends Application {

  private Scene scene;

  @Override
  public void init() throws IOException {
    FXMLLoader loader
            = new FXMLLoader(ShuffleboardController.class.getResource("Shuffleboard.fxml"));
    scene = new Scene(loader.load());
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    checkNotNull(primaryStage);

    primaryStage.setScene(scene);
    primaryStage.setTitle("shuffleboard");
    primaryStage.show();
  }

}
