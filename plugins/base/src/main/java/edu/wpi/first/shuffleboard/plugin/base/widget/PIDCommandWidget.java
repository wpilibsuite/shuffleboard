package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SubSource;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.PIDCommandData;
import edu.wpi.first.shuffleboard.plugin.base.data.PIDControllerData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.PIDControllerType;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

@Description(name = "PID Command", dataTypes = PIDCommandData.class)
@ParametrizedController("PIDCommandWidget.fxml")
public class PIDCommandWidget extends SimpleAnnotatedWidget<PIDCommandData> {

  @FXML
  private Pane root;
  @FXML
  private CheckBox checkbox;

  private DataSource<PIDControllerData> controllerDataSource;

  // Use a PIDControllerWidget to control the PID stuff since we've already created a robust widget for it
  // No need to re-implement it for this widget
  private PIDControllerWidget controller;

  @FXML
  private void initialize() throws IOException {
    FXMLLoader controllerLoader = new FXMLLoader(PIDControllerWidget.class.getResource("PIDControllerWidget.fxml"));
    controllerLoader.load();
    controller = controllerLoader.getController();

    root.getChildren().add(controller.getView());

    dataOrDefault.addListener((__, prev, cur) -> checkbox.setSelected(cur.isRunning()));
    checkbox.selectedProperty().addListener((__, was, is) -> setData(dataOrDefault.get().withRunning(is)));

    typedSourceProperty().addListener((__, oldSource, newSource) -> {
      controllerDataSource = new SubSource<>(
          PIDControllerType.Instance,
          newSource,
          pidControllerData -> new PIDCommandData(dataOrDefault.get().getCommandData(), pidControllerData),
          PIDCommandData::getPidControllerData
      );
      controller.setSource(controllerDataSource);
    });
  }

  @Override
  public Pane getView() {
    return root;
  }

}
