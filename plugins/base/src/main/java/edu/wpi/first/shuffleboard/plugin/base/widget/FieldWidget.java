package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.FieldData;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Field", dataTypes = FieldData.class)
@ParametrizedController("FieldWidget.fxml")
public class FieldWidget extends SimpleAnnotatedWidget<FieldData> {
  @FXML
  Pane root;

  @FXML
  Label xLabel;

  @FXML
  private void initialize() {
    root.widthProperty().addListener((__, ___, width) -> {
      xLabel.setTranslateX(dataOrDefault.get().getRobot().getX() - width.doubleValue() / 2);
    });
    root.heightProperty().addListener((__, ___, height) -> {
      xLabel.setTranslateY(dataOrDefault.get().getRobot().getY() + height.doubleValue());
    });
    dataOrDefault.addListener((__, ___, data) -> {
      xLabel.setTranslateX(data.getRobot().getX() - root.getWidth() / 2);
      xLabel.setTranslateY(data.getRobot().getY() + root.getHeight());
      xLabel.setRotate(-data.getRobot().getDegrees());
    });
  }

  @Override
  public Pane getView() {
    return root;
  }
}
