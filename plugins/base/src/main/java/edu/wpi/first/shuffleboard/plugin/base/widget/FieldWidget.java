package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.FieldData;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

@Description(name = "Field", dataTypes = FieldData.class)
@ParametrizedController("FieldWidget.fxml")
public class FieldWidget extends SimpleAnnotatedWidget<FieldData> {
  @FXML
  Pane root;

  @FXML
  StackPane pane;

  @FXML
  ImageView backgroundImage, robot;

  @FXML
  private void initialize() {
    backgroundImage.setImage(new Image(getClass().getResource("2018-field.jpg").toExternalForm()));

    root.widthProperty().addListener((__, ___, width) -> {
      robot.setTranslateX(dataOrDefault.get().getRobot().getX() - width.doubleValue() / 2);
      backgroundImage.setFitWidth(width.doubleValue());
    });
    root.heightProperty().addListener((__, ___, height) -> {
      robot.setTranslateY(dataOrDefault.get().getRobot().getY() + height.doubleValue());
      backgroundImage.setFitHeight(height.doubleValue());
    });
    dataOrDefault.addListener((__, ___, data) -> {
      robot.setTranslateX(data.getRobot().getX() - root.getWidth() / 2);
      robot.setTranslateY(data.getRobot().getY() + root.getHeight());
      robot.setRotate(-data.getRobot().getDegrees());
    });
  }

  @Override
  public Pane getView() {
    return root;
  }
}
