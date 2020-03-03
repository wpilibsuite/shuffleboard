package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import com.google.common.collect.ImmutableList;

import java.util.List;

import org.fxmisc.easybind.EasyBind;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(
    name = "Number Slider",
    dataTypes = Number.class)
@ParametrizedController("NumberSliderWidget.fxml")
public class NumberSliderWidget extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  private Slider slider;
  @FXML
  private Label text;

  private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", true);

  @FXML
  private void initialize() {
    // enforce five evenly-spaced ticks at all times
    slider.majorTickUnitProperty().bind(
        slider.maxProperty()
              .subtract(slider.minProperty())
              .divide(4));
    slider.valueProperty().bindBidirectional(dataProperty());
    text.textProperty().bind(EasyBind.map(dataOrDefault, n -> String.format("%.2f", n.doubleValue())));
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Slider Settings",
            Setting.of("Min", slider.minProperty(), Double.class),
            Setting.of("Max", slider.maxProperty(), Double.class),
            Setting.of("Block increment", slider.blockIncrementProperty(), Double.class)
        ),
        Group.of("Visuals",
            Setting.of("Display value", showText, Boolean.class)
        )
    );
  }

  public boolean isShowText() {
    return showText.get();
  }

  public BooleanProperty showTextProperty() {
    return showText;
  }

  public void setShowText(boolean showText) {
    this.showText.set(showText);
  }
}
