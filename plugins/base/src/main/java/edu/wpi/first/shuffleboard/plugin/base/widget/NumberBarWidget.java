package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;


import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Number Bar", dataTypes = {Number.class})
@ParametrizedController("NumberBar.fxml")
public class NumberBarWidget extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator indicator;
  @FXML
  private Label text;

  private final DoubleProperty numTicks = new SimpleDoubleProperty(this, "numTickMarks", 5);
  private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", true);

  @FXML
  private void initialize() {
    indicator.valueProperty().bind(dataOrDefault);
    text.textProperty().bind(EasyBind.map(dataOrDefault, n -> String.format("%.2f", n.doubleValue())));

    indicator.majorTickUnitProperty().bind(
        EasyBind.combine(indicator.minProperty(), indicator.maxProperty(), numTicks,
            (min, max, numTicks) -> {
              if (numTicks.intValue() > 1) {
                return (max.doubleValue() - min.doubleValue()) / (numTicks.intValue() - 1);
              } else {
                return max.doubleValue() - min.doubleValue();
              }
            }));
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Range",
            Setting.of("Min", indicator.minProperty(), Double.class),
            Setting.of("Max", indicator.maxProperty(), Double.class),
            Setting.of("Center", indicator.centerProperty(), Double.class)
        ),
        Group.of("Visuals",
            Setting.of("Num tick marks", numTicks, Integer.class),
            Setting.of("Show text", showText, Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
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
