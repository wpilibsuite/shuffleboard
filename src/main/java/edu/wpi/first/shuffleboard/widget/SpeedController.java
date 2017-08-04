package edu.wpi.first.shuffleboard.widget;

import com.google.common.primitives.Doubles;

import edu.wpi.first.shuffleboard.components.LinearIndicator;
import edu.wpi.first.shuffleboard.components.NumberField;
import edu.wpi.first.shuffleboard.data.SpeedControllerData;
import edu.wpi.first.shuffleboard.data.types.SpeedControllerType;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

@Description(name = "Speed Controller", dataTypes = SpeedControllerType.class)
@ParametrizedController("SpeedController.fxml")
public class SpeedController extends SimpleAnnotatedWidget<SpeedControllerData> {

  private final BooleanProperty viewOnly = new SimpleBooleanProperty(this, "viewOnly", true);

  @FXML
  private StackPane root;
  @FXML
  private Pane viewPane;
  @FXML
  private Pane controlPane;
  @FXML
  private LinearIndicator view;
  @FXML
  private Slider control;
  @FXML
  private NumberField valueField;

  private final ChangeListener<? super Number> numberUpdateListener = (__, prev, cur) -> {
    double value = Doubles.constrainToRange(cur.doubleValue(), -1, 1);
    setData(new SpeedControllerData(getData().getName(), value));
  };

  @FXML
  private void initialize() {
    viewPane.visibleProperty().bind(viewOnly);
    controlPane.visibleProperty().bind(viewOnly.not());
    view.valueProperty().bind(EasyBind.monadic(dataProperty()).map(SpeedControllerData::getValue));
    control.valueProperty().addListener(numberUpdateListener);
    valueField.numberProperty().addListener(numberUpdateListener);
    dataProperty().addListener((__, prev, cur) -> {
      control.setValue(cur.getValue());
      valueField.setNumber(cur.getValue());
    });
    exportProperties(viewOnly);
  }

  @Override
  public Pane getView() {
    return root;
  }

  @FXML
  private void zero() {
    control.setValue(0); // listeners will take care of the rest
  }

}
