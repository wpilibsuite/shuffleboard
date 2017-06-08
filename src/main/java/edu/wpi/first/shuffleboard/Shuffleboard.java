package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

@SuppressWarnings("JavadocMethod")
public class Shuffleboard extends Application {

  private Pane mainPane;

  public static void main(String[] args) {
    NetworkTablesJNI.startClient("localhost", 1735);
    launch(args);
  }

  @Override
  public void init() throws Exception {
    Widgets.discover();

    mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    primaryStage.setScene(new Scene(mainPane));
    primaryStage.show();
  }

}
