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
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

@Description(name = "Field", dataTypes = FieldData.class)
@SuppressWarnings({"EmptyCatchBlock", "PMD.TooManyFields"})
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

  private final Map<String, Paint> colors = new HashMap<>();
  private final Map<String, Circle[]> objectCircles = new HashMap<>();
  private Map<String, FieldData.SimplePose2d[]> previousObjects = new HashMap<>();
  private final Property<Game> game =
          new SimpleObjectProperty<>(Game.A2022_Rapid_React);
  private final DoubleProperty robotSize = new SimpleDoubleProperty(50);
  private final BooleanProperty showCirclesOutsideOfField = new SimpleBooleanProperty(false);


  @FXML
  private void initialize() {
    setGame(game.getValue());
    game.addListener(__ -> {
      setGame(game.getValue());
      centerImage();
      updateRobotPosition();
      updateObjects(true);
    });

    robot.setImage(
            new Image(getClass().getResource("field/robot.png").toExternalForm()));
    robot.setFitWidth(robotSize.get());
    robot.setFitHeight(robotSize.get());
    robotSize.addListener(__ -> {
      double size = robotSize.get();
      robot.setFitWidth(size);
      robot.setFitHeight(size);
      backgroundImage.setFitHeight(root.getHeight() - robotSize.get() / 2);
      backgroundImage.setFitWidth(root.getWidth() - robotSize.get() / 2);
      updateRobotPosition();
    });

    showCirclesOutsideOfField.addListener(__ -> updateObjects(true));

    root.heightProperty().addListener(__ -> {
      double height = root.getHeight();
      backgroundImage.setFitHeight(height - robotSize.get() / 2);
      centerImage();
      updateRobotPosition();
      updateObjects(true);
    });
    root.widthProperty().addListener(__ -> {
      double width = root.getWidth();
      backgroundImage.setFitWidth(width - robotSize.get() / 2);
      centerImage();
      updateRobotPosition();
      updateObjects(true);
    });

    dataOrDefault.addListener(__ -> {
      updateRobotPosition();
      updateObjects(false);
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
      backgroundImage.setX(robotSize.get() / 4);
      backgroundImage.setY((backgroundImage.getFitHeight()
              - backgroundImage.getFitWidth() * imageRatio)
              / 2
              + robotSize.get() / 4);
    } else {
      backgroundImage.setX((backgroundImage.getFitWidth()
              - backgroundImage.getFitHeight() / imageRatio)
              / 2
              + robotSize.get() / 4);
      backgroundImage.setY(robotSize.get() / 4);
    }
  }

  private void setGame(Game game) {
    InputStream stream = getClass().getResourceAsStream(game.json());

    try {
      if (stream == null) {
        throw new Exception("Cannot read JSON of " + game);
      }
      Gson gson = new Gson();
      Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
      Map<?, ?> map = gson.fromJson(reader, Map.class);

      URL imagePath = getClass()
              .getResource("field/" + map.get("field-image"));
      if (imagePath == null) {
        throw new Exception("Cannot get image at " + Paths.get("field", (String) map.get("field-image")));
      }
      Image image = new Image(imagePath.toExternalForm());
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
    } finally {
      try {
        if (stream != null) {
          stream.close();
        }
      } catch (IOException ignored) { }
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
            transformX(dataOrDefault.get().getRobot().getX(), robotSize.get() / 2));
    robot.setTranslateY(
            transformY(dataOrDefault.get().getRobot().getY(), robotSize.get() / 2));
    robot.setRotate(-dataOrDefault.get().getRobot().getDegrees());
  }

  private void updateObjects(boolean forceUpdateObjects) {
    var newObjects = dataOrDefault.get().getObjects();
    for (Map.Entry<String, FieldData.SimplePose2d[]> entry : dataOrDefault.get().getObjects().entrySet()) {
      String key = entry.getKey();
      var newObject = entry.getValue();

      if (!forceUpdateObjects && previousObjects.containsKey(key)) {
        boolean changed = false;
        var previousObject = previousObjects.get(key);
        if (previousObject.length == newObject.length) {
          for (int i = 0; i < previousObject.length; i++) {
            if (!previousObject[i].equals(newObject[i])) {
              changed = true;
              break;
            }
          }
        } else {
          changed = true;
        }

        if (!changed) {
          continue;
        }
      }

      for (var circle : objectCircles.getOrDefault(key, new Circle[0])) {
        pane.getChildren().remove(circle);
      }

      if (!colors.containsKey(key)) {
        colors.put(key, Color.valueOf("#ffffff"));
      }

      var newCircles = new Circle[newObject.length];
      for (int i = 0; i < newObject.length; i++) {
        var pose = newObject[i];

        if (!showCirclesOutsideOfField.get() && (
                pose.getX() < 0
                || pose.getY() < 0
                || pose.getX() > fieldWidth
                || pose.getY() > fieldHeight)) {
          continue;
        }

        Paint paint;
        try {
          paint = colors.get(key);
        } catch (Exception ignored) {
          paint = Paint.valueOf("#ffffff");
        }

        newCircles[i] = new Circle(transformX(pose.getX(), 1.25),
                transformY(pose.getY(), 1.25), 2.5,
                paint);

        pane.getChildren().add(newCircles[i]);
      }
      objectCircles.put(key, newCircles);
    }
    previousObjects = newObjects;
  }

  @Override
  public List<Group> getSettings() {
    List<Setting<?>> colorSettings = new ArrayList<>();
    for (Map.Entry<String, Paint> entry : colors.entrySet()) {
      Property<Paint> property = new SimpleObjectProperty<>(entry.getValue());
      property.addListener(__ -> {
        colors.put(entry.getKey(), property.getValue());
        updateObjects(true);
      });
      colorSettings.add(Setting.of(entry.getKey(), property, Color.class));
    }

    return ImmutableList.of(
            Group.of("Game", Setting.of("Game", game, Game.class)),
            Group.of("Visuals",
                    Setting.of("Robot Icon Size", robotSize, Double.class),
                    Setting.of("Show Outside Circles", showCirclesOutsideOfField, Boolean.class)
            ),
            Group.of("Colors", colorSettings)
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  private enum Game {
    A2018_Power_Up,
    A2019_Deep_Space,
    A2020_Infinite_Recharge,
    A2021_Barrel_Racing_Path,
    A2021_Bounce_Path,
    A2021_Galactic_Search_A,
    A2021_Galactic_Search_B,
    A2021_Infinite_Recharge,
    A2021_Slalom_Path,
    A2022_Rapid_React;

    public String json() {
      return "field/" + this.name().substring(1).toLowerCase().replaceFirst("_", "-").replaceAll("_", "") + ".json";
    }

    @Override
    public String toString() {
      return this.name().substring(1).replaceAll("_", " ");
    }
  }
}
