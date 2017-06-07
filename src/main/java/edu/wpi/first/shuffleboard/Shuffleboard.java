package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.widget.StockWidgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
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
    // Load the Roboto font
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Regular.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Bold.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Italic.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-BoldItalic.ttf").openStream(), -1);
    StockWidgets.init();
    mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    primaryStage.setScene(new Scene(mainPane));
    primaryStage.show();
  }

}
