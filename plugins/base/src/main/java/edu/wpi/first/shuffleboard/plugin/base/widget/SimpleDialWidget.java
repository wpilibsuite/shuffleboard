package edu.wpi.first.shuffleboard.plugin.base.widget;

import eu.hansolo.medusa.Gauge;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Simple Dial", dataTypes = Number.class)
@ParametrizedController("SimpleDialWidget.fxml")
public class SimpleDialWidget extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  private Gauge dial;

  @FXML
  private void initialize() {
    dial.valueProperty().bind(dataOrDefault);
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Range",
            Setting.of("Min", dial.minValueProperty(), Double.class),
            Setting.of("Max", dial.maxValueProperty(), Double.class)
        ),
        Group.of("Visuals",
            Setting.of("Show value", dial.valueVisibleProperty(), Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

}
