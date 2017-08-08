package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.app.widget.Widgets;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Screen;
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
    // Load the Roboto font
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Regular.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Bold.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-Italic.ttf").openStream(), -1);
    Font.loadFont(getClass().getResource("font/roboto/Roboto-BoldItalic.ttf").openStream(), -1);

    Widgets.discover();
    Recorder.getInstance().start();

    mainPane = FXMLLoader.load(MainWindowController.class.getResource("MainWindow.fxml"));
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    primaryStage.setScene(new Scene(mainPane));
    primaryStage.setMinWidth(640);
    primaryStage.setMinHeight(480);
    primaryStage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
    primaryStage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());
    primaryStage.show();
  }

}
