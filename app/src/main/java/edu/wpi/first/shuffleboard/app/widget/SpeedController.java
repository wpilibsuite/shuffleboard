package edu.wpi.first.shuffleboard.app.widget;

import com.google.common.primitives.Doubles;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.data.SpeedControllerData;
import edu.wpi.first.shuffleboard.api.data.types.SpeedControllerType;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.app.LiveWindow;

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

  private final BooleanProperty controllable = new SimpleBooleanProperty(this, "controllable", true);

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
    controllable.set(LiveWindow.isEnabled());

    LiveWindow.enabledProperty().addListener((__, was, is) -> controllable.set(is));
    viewPane.visibleProperty().bind(controllable.not());
    controlPane.visibleProperty().bind(controllable);

    view.valueProperty().bind(EasyBind.monadic(dataProperty()).map(SpeedControllerData::getValue));

    control.valueProperty().addListener(numberUpdateListener);
    valueField.numberProperty().addListener(numberUpdateListener);
    dataProperty().addListener((__, prev, cur) -> {
      control.setValue(cur.getValue());
      valueField.setNumber(cur.getValue());
    });

    exportProperties(controllable);
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
