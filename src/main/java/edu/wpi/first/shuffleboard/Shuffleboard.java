package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.widget.StockWidgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

@SuppressWarnings("JavadocMethod")
public class Shuffleboard extends Application {

  private Pane mainPane;

  public static void main(String[] args) {
    NetworkTablesJNI.startClient("localhost", 1735);
    launch(args);
  }

  @Override
  public void init() throws Exception {
    StockWidgets.init();

    mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    primaryStage.setScene(new Scene(mainPane));
    primaryStage.show();
  }

}
