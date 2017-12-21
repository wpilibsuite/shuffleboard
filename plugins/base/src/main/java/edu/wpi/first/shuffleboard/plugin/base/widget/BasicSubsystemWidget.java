package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.BasicSubsystemData;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Basic Subsystem", dataTypes = BasicSubsystemData.class)
@ParametrizedController("BasicSubsystemWidget.fxml")
public final class BasicSubsystemWidget extends SimpleAnnotatedWidget<BasicSubsystemData> {

  @FXML
  private Pane root;
  @FXML
  private Label defaultCommandLabel;
  @FXML
  private Label currentCommandLabel;

  private MonadicBinding<String> defaultCommandText; // NOPMD could be local variable -- in a field to avoid GC
  private MonadicBinding<String> currentCommandText; // NOPMD could be local variable -- in a field to avoid GC

  @FXML
  private void initialize() {
    defaultCommandText = EasyBind.monadic(dataOrDefault)
        .map(BasicSubsystemData::getDefaultCommandName)
        .map(n -> n.isEmpty() ? "None" : n)
        .map(n -> "Default command: " + n);
    currentCommandText = EasyBind.monadic(dataOrDefault)
        .map(BasicSubsystemData::getCurrentCommandName)
        .map(n -> n.isEmpty() ? "None" : n)
        .map(n -> "Current command: " + n);

    defaultCommandLabel.textProperty().bind(defaultCommandText);
    currentCommandLabel.textProperty().bind(currentCommandText);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
