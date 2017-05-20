package edu.wpi.first.shuffleboard;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

  public static void main(String[] args) {
    NetworkTable.setClientMode();
    NetworkTable.setIPAddress("localhost"); // for local testing
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    StockWidgets.init();

    Pane mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
    primaryStage.setScene(new Scene(mainPane));
    primaryStage.show();
  }

}
