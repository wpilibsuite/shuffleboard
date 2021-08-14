package edu.wpi.first.shuffleboard.plugin.base.widget;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.FieldData;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

@Description(name = "Field", dataTypes = FieldData.class)
@SuppressWarnings("EmptyCatchBlock")
@ParametrizedController("FieldWidget.fxml")
public class FieldWidget extends SimpleAnnotatedWidget<FieldData> {
  @FXML
  private Pane root;

  @FXML
  private BorderPane pane;

  @FXML
  private ImageView backgroundImage;

  @FXML
  private ImageView robot;

  private double imageStartX;
  private double imageStartY;
  private double imageEndX;
  private double imageEndY;
  private double fieldWidth;
  private double fieldHeight;

  private enum Game {
    A2018_Power_Up,
    A2019_Deep_Space,
    A2020_Infinite_Recharge,
    A2021_Barrel_Racing_Path,
    A2021_Bounce_Path,
    A2021_Galactic_Search_A,
    A2021_Galactic_Search_B,
    A2021_Infinite_Recharge,
    A2021_Slalom_Path;

    public String json() {
      return "field/" + this.name().substring(1).toLowerCase().replaceFirst("_", "-").replaceAll("_", "") + ".json";
    }

    @Override
    public String toString() {
      return this.name().substring(1).replaceAll("_", " ");
    }
  }

  private Property<Game> game =
          new SimpleObjectProperty<>(Game.A2021_Infinite_Recharge);

  private Map<String, String> colors = new HashMap<>();

  private static final double ROBOT_SIZE = 30;

  @FXML
  private void initialize() {
    robot.setImage(
            new Image(getClass().getResource("field/robot.png").toExternalForm()));
    robot.setFitWidth(ROBOT_SIZE);
    robot.setFitHeight(ROBOT_SIZE);
    setGame(game.getValue());
    game.addListener((__, ___, newGame) -> {
      setGame(newGame);
      centerImage();
      updateRobotPosition();
      updateObjects();
    });

    root.heightProperty().addListener((__, ___, height) -> {
      backgroundImage.setFitHeight(height.doubleValue() - ROBOT_SIZE / 2);
      centerImage();
      updateRobotPosition();
      updateObjects();
    });
    root.widthProperty().addListener((__, ___, width) -> {
      backgroundImage.setFitWidth(width.doubleValue() - ROBOT_SIZE / 2);
      centerImage();
      updateRobotPosition();
      updateObjects();
    });

    dataOrDefault.addListener((__, ___, data) -> {
      updateRobotPosition();
      updateObjects();
    });
  }

  private double getActualBackgroundHeight() {
    return Math.min(backgroundImage.getFitHeight(),
            backgroundImage.getFitWidth()
                    * (backgroundImage.getImage().getHeight()
                    / backgroundImage.getImage().getWidth()));
  }

  private double getActualBackgroundWidth() {
    return Math.min(backgroundImage.getFitWidth(),
            backgroundImage.getFitHeight()
                    / ((backgroundImage.getImage().getHeight()
                    / backgroundImage.getImage().getWidth())));
  }

  private void centerImage() {
    double imageRatio = backgroundImage.getImage().getHeight() / backgroundImage.getImage().getWidth();
    if (backgroundImage.getFitWidth() * imageRatio
            < backgroundImage.getFitHeight()) {
      backgroundImage.setX(ROBOT_SIZE / 4);
      backgroundImage.setY((backgroundImage.getFitHeight()
              - backgroundImage.getFitWidth() * imageRatio)
              / 2
              + ROBOT_SIZE / 4);
    } else {
      backgroundImage.setX((backgroundImage.getFitWidth()
              - backgroundImage.getFitHeight() / imageRatio)
              / 2
              + ROBOT_SIZE / 4);
      backgroundImage.setY(ROBOT_SIZE / 4);
    }
  }

  @Override
  public Pane getView() {
    return root;
  }

  private void setGame(Game game) {
    try {
      String jsonPath = getClass().getResource(game.json()).toExternalForm();
      Gson gson = new Gson();
      InputStream stream = getClass().getResourceAsStream(game.json());
      if (stream == null) {
        throw new Exception("Cannot read JSON at " + jsonPath);
      }
      Reader reader = new BufferedReader(new InputStreamReader(stream));
      Map<?, ?> map = gson.fromJson(reader, Map.class);

      String directory =
              Paths.get(jsonPath).getParent().getFileName().toString();

      Image image = new Image(
              getClass()
                      .getResource(Paths.get(directory, (String) map.get("field-image")).toString())
                      .toExternalForm());
      System.out.println("FLAG: " + image.getUrl());
      backgroundImage.setImage(image);

      imageStartX =
              ((List<Double>) ((Map<?, ?>) map.get("field-corners")).get("top-left"))
                      .get(0);
      imageEndX = ((List<Double>) ((Map<?, ?>) map.get("field-corners"))
              .get("bottom-right"))
              .get(0);
      imageStartY = image.getHeight() 
              - ((List<Double>) ((Map<?, ?>) map.get("field-corners"))
                      .get("bottom-right"))
                      .get(1);
      imageEndY =
              image.getHeight() 
                      - ((List<Double>) ((Map<?, ?>) map.get("field-corners")).get("top-left"))
                              .get(1);

      fieldWidth = ((List<Double>) map.get("field-size")).get(0);
      fieldHeight = ((List<Double>) map.get("field-size")).get(1);

      String fieldUnit = (String) map.get("field-unit");
      if (fieldUnit.equals("feet") || fieldUnit.equals("foot")) {
        fieldWidth = UltrasonicWidget.Unit.FOOT.as(fieldWidth,
                UltrasonicWidget.Unit.METER);
        fieldHeight = UltrasonicWidget.Unit.FOOT.as(
                fieldHeight, UltrasonicWidget.Unit.METER);
      }
    } catch (Exception ignored) {
    }
  }

  private double transformX(double robotX, double size) {
    return backgroundImage.getX()
            + (imageStartX + robotX / fieldWidth * (imageEndX - imageStartX))
            * getActualBackgroundWidth() / backgroundImage.getImage().getWidth()
            - size;
  }

  private double transformY(double robotY, double size) {
    return backgroundImage.getY() + getActualBackgroundHeight()
            - (imageStartY + robotY / fieldHeight * (imageEndY - imageStartY))
                    * getActualBackgroundHeight()
                    / backgroundImage.getImage().getHeight() - size;
  }

  private void updateRobotPosition() {
    robot.setTranslateX(
            transformX(dataOrDefault.get().getRobot().getX(), ROBOT_SIZE / 2));
    robot.setTranslateY(
            transformY(dataOrDefault.get().getRobot().getY(), ROBOT_SIZE / 2));
    robot.setRotate(-dataOrDefault.get().getRobot().getDegrees());
  }

  private void updateObjects() {
    pane.getChildren().removeIf(n -> n instanceof Circle);

    for (String object : dataOrDefault.get().getObjects().keySet()) {
      if (!colors.containsKey(object)) {
        colors.put(object, "#ffffff");
      }
      for (FieldData.SimplePose2d pose :
              dataOrDefault.get().getObjects().get(object)) {
        Paint paint;
        try {
          paint = Paint.valueOf(colors.get(object));
        } catch (Exception ignored) {
          paint = Paint.valueOf("#ffffff");
        }
        pane.getChildren().add(new Circle(transformX(pose.getX(), 1.25),
                transformY(pose.getY(), 1.25), 2.5,
                paint));
      }
    }
  }

  @Override
  public List<Group> getSettings() {
    List<Setting<?>> colorSettings = new ArrayList<>();
    for (String object : colors.keySet()) {
      StringProperty property = new SimpleStringProperty(colors.get(object));
      property.addListener((__, ___, newColor) -> {
        colors.put(object, newColor);
        updateObjects();
      });
      colorSettings.add(Setting.of(object, property, String.class));
    }

    return ImmutableList.of(
            Group.of("Field", Setting.of("Game", game, Game.class)),
            Group.of("Colors", colorSettings));
  }
}
