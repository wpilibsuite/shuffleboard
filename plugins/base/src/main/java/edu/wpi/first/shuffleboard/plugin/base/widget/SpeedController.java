package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.LiveWindow;
import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.SpeedControllerData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SpeedControllerType;

import com.google.common.primitives.Doubles;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
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

  private final Property<Orientation> orientation =
      new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL);

  @FXML
  private void initialize() {
    controllable.set(LiveWindow.isEnabled());

    LiveWindow.enabledProperty().addListener((__, was, is) -> controllable.set(is));
    viewPane.visibleProperty().bind(controllable.not());
    controlPane.visibleProperty().bind(controllable);

    view.valueProperty().bind(EasyBind.monadic(dataProperty()).map(SpeedControllerData::getValue));

    control.valueProperty().addListener(numberUpdateListener);
    valueField.numberProperty().addListener(numberUpdateListener);
    dataOrDefault.addListener((__, prev, cur) -> {
      control.setValue(cur.getValue());
      valueField.setNumber(cur.getValue());
    });
    control.orientationProperty().bind(orientation);
    view.orientationProperty().bind(orientation);

    exportProperties(controllable, orientation);
  }

  @Override
  public Pane getView() {
    return root;
  }

  @FXML
  private void zero() {
    control.setValue(0); // listeners will take care of the rest
  }

  public Orientation getOrientation() {
    return orientation.getValue();
  }

  public Property<Orientation> orientationProperty() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation.setValue(orientation);
  }

}
