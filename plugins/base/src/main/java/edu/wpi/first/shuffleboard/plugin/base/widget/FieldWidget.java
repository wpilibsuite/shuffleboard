package edu.wpi.first.shuffleboard.plugin.base.widget;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.FieldData;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Description(name = "Field", dataTypes = FieldData.class)
@ParametrizedController("FieldWidget.fxml")
public class FieldWidget extends SimpleAnnotatedWidget<FieldData> {
    @FXML
    Pane root;

    @FXML
    BorderPane pane;

    @FXML
    ImageView backgroundImage, robot;

    double imageStartX, imageStartY, imageEndX, imageEndY, fieldWidth, fieldHeight;

    StringProperty game = new SimpleStringProperty("field/2021-infiniterecharge.json");

    private static final double ROBOT_SIZE = 30;

    @FXML
    private void initialize() {
        robot.setImage(new Image(getClass().getResource("field/robot.png").toExternalForm()));
        robot.setFitWidth(ROBOT_SIZE);
        robot.setFitHeight(ROBOT_SIZE);
        setGame(game.get());
        game.addListener((__, ___, newGame) -> {
            setGame(newGame);
            centerImage();
            updateRobotPosition();
        });

        root.heightProperty().addListener((__, ___, height) -> {
            backgroundImage.setFitHeight(height.doubleValue() - ROBOT_SIZE / 2);
            centerImage();
            updateRobotPosition();
        });
        root.widthProperty().addListener((__, ___, width) -> {
            backgroundImage.setFitWidth(width.doubleValue() - ROBOT_SIZE / 2);
            centerImage();
            updateRobotPosition();
        });

        dataOrDefault.addListener((__, ___, data) -> updateRobotPosition());
    }

    private double getActualBackgroundHeight() {
        return Math.min(backgroundImage.getFitHeight(), backgroundImage.getFitWidth() * (backgroundImage.getImage().getHeight() / backgroundImage.getImage().getWidth()));
    }

    private double getActualBackgroundWidth() {
        return Math.min(backgroundImage.getFitWidth(), backgroundImage.getFitHeight() / ((backgroundImage.getImage().getHeight() / backgroundImage.getImage().getWidth())));
    }

    private void centerImage() {
        double imageRatio = (backgroundImage.getImage().getHeight() / backgroundImage.getImage().getWidth());
        if (backgroundImage.getFitWidth() * imageRatio < backgroundImage.getFitHeight()) {
            backgroundImage.setX(ROBOT_SIZE / 4);
            backgroundImage.setY((backgroundImage.getFitHeight() - backgroundImage.getFitWidth() * imageRatio) / 2 + ROBOT_SIZE / 4);
        } else {
            backgroundImage.setX((backgroundImage.getFitWidth() - backgroundImage.getFitHeight() / imageRatio) / 2 + ROBOT_SIZE / 4);
            backgroundImage.setY(ROBOT_SIZE / 4);
        }
    }

    @Override
    public Pane getView() {
        return root;
    }

    private void setGame(String jsonPath) {
        try {
            Gson gson = new Gson();
            InputStream stream = getClass().getResourceAsStream(jsonPath);
            if (stream == null) {
                throw new Exception("Cannot read JSON at " + jsonPath);
            }
            Reader reader = new BufferedReader(new InputStreamReader(stream));
            Map<?, ?> map = gson.fromJson(reader, Map.class);

            String directory = Paths.get(jsonPath).getParent().getFileName().toString();

            Image image = new Image(getClass().getResource(Paths.get(directory, (String) map.get("field-image")).toString()).toExternalForm());
            backgroundImage.setImage(image);

            imageStartX = ((List<Double>) ((Map<?, ?>) map.get("field-corners")).get("top-left")).get(0);
            imageEndX = ((List<Double>) ((Map<?, ?>) map.get("field-corners")).get("bottom-right")).get(0);
            imageStartY = image.getHeight() - ((List<Double>) ((Map<?, ?>) map.get("field-corners")).get("bottom-right")).get(1);
            imageEndY = image.getHeight() - ((List<Double>) ((Map<?, ?>) map.get("field-corners")).get("top-left")).get(1);

            fieldWidth = ((List<Double>) map.get("field-size")).get(0);
            fieldHeight = ((List<Double>) map.get("field-size")).get(1);

            String fieldUnit = ((String) map.get("field-unit"));
            if (fieldUnit.equals("feet") || fieldUnit.equals("foot")) {
                fieldWidth = UltrasonicWidget.Unit.FOOT.as(fieldWidth, UltrasonicWidget.Unit.METER);
                fieldHeight = UltrasonicWidget.Unit.FOOT.as(fieldHeight, UltrasonicWidget.Unit.METER);
            }
        } catch (Exception ignored) {
        }
    }

    private double transformX(double robotX) {
        return backgroundImage.getX() + (imageStartX + robotX / fieldWidth * (imageEndX - imageStartX)) * getActualBackgroundWidth() / backgroundImage.getImage().getWidth() - ROBOT_SIZE / 2;
    }

    private double transformY(double robotY) {
        return backgroundImage.getY() + getActualBackgroundHeight() - (imageStartY + robotY / fieldHeight * (imageEndY - imageStartY)) * getActualBackgroundHeight() / backgroundImage.getImage().getHeight() - ROBOT_SIZE / 2;
    }

    private void updateRobotPosition() {
        robot.setTranslateX(transformX(dataOrDefault.get().getRobot().getX()));
        robot.setTranslateY(transformY(dataOrDefault.get().getRobot().getY()));
        robot.setRotate(-dataOrDefault.get().getRobot().getDegrees());
    }

    @Override
    public List<Group> getSettings() {
        return ImmutableList.of(
                Group.of("Field", Setting.of("Game", game, String.class))
        );
    }
}
