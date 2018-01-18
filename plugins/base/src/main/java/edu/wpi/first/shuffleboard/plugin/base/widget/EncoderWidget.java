package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.EncoderData;

import org.fxmisc.easybind.EasyBind;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

@Description(name = "Encoder", dataTypes = {EncoderData.class})
@ParametrizedController("Encoder.fxml")
public class EncoderWidget extends SimpleAnnotatedWidget<EncoderData> {

  @FXML
  private Pane root;
  @FXML
  private TextField distanceField;
  @FXML
  private TextField speedField;

  @FXML
  private void initialize() {
    distanceField.textProperty().bind(
        EasyBind.monadic(dataProperty())
            .map(EncoderData::getDistance)
            .map(Object::toString));
    speedField.textProperty().bind(
        EasyBind.monadic(dataProperty())
            .map(EncoderData::getSpeed)
            .map(Object::toString));
  }

  @Override
  public Pane getView() {
    return root;
  }

}
