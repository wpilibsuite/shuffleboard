package edu.wpi.first.shuffleboard.plugin.cameraserver.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;

import org.fxmisc.easybind.EasyBind;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

@Description(name = "Camera Stream", dataTypes = CameraServerData.class)
@ParametrizedController("CameraServerWidget.fxml")
public class CameraServerWidget extends SimpleAnnotatedWidget<CameraServerData> {

  @FXML
  private Pane root;
  @FXML
  private ImageView imageView;
  @FXML
  private Image emptyImage;

  @FXML
  private void initialize() {
    imageView.imageProperty().bind(EasyBind.monadic(dataProperty()).map(CameraServerData::getImage).orElse(emptyImage));
  }

  @Override
  public Pane getView() {
    return root;
  }

}
