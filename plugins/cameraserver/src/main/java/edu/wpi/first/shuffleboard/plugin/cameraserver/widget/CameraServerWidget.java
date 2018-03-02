package edu.wpi.first.shuffleboard.plugin.cameraserver.widget;

import edu.wpi.first.shuffleboard.api.components.IntegerField;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSource;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

@Description(name = "Camera Stream", dataTypes = CameraServerData.class)
@ParametrizedController("CameraServerWidget.fxml")
public class CameraServerWidget extends SimpleAnnotatedWidget<CameraServerData> {

  @FXML
  private Pane root;
  //  @FXML
//  private Label fpsLabel;
//  @FXML
//  private Label bandwidthLabel;
  @FXML
  private ImageView imageView;
  @FXML
  private Image emptyImage;
  @FXML
  private Pane controls;
  @FXML
  @SavePropertyFrom(propertyName = "value", savedName = "compression")
  private Slider compressionSlider;
  @FXML
  @SavePropertyFrom(propertyName = "number", savedName = "fps")
  private IntegerField frameRateField;
  @FXML
  @SavePropertyFrom(propertyName = "number", savedName = "imageWidth")
  private IntegerField width;
  @FXML
  @SavePropertyFrom(propertyName = "number", savedName = "imageHeight")
  private IntegerField height;
  @FXML
  private Node crosshairs;

  private final BooleanProperty showControls = new SimpleBooleanProperty(this, "showControls", true);
  private final BooleanProperty showCrosshair = new SimpleBooleanProperty(this, "showCrosshair", true);
  private final Property<Color> crosshairColor = new SimpleObjectProperty<>(this, "crosshairColor", Color.WHITE);
  private final ChangeListener<Number> sourceCompressionListener =
      (__, old, compression) -> compressionSlider.setValue(compression.doubleValue());
  private final ChangeListener<Number> numberChangeListener =
      (__, old, fps) -> frameRateField.setNumber(fps.intValue());
  private final ChangeListener<Resolution> resolutionChangeListener =
      (__, old, resolution) -> {
        width.setNumber(resolution.getWidth());
        height.setNumber(resolution.getHeight());
      };

  @FXML
  private void initialize() {
    imageView.imageProperty().bind(dataOrDefault.map(CameraServerData::getImage).orElse(emptyImage));

    sourceProperty().addListener((__, old, source) -> {
      if (source instanceof CameraServerSource) {
        CameraServerSource s = (CameraServerSource) source;
        if (source.hasClients()) {
          compressionSlider.setValue(s.getTargetCompression());
          frameRateField.setNumber(s.getTargetFps());
          width.setNumber(s.getTargetResolution().getWidth());
          height.setNumber(s.getTargetResolution().getHeight());
        } else {
          applySettings();
        }
        s.targetCompressionProperty().addListener(sourceCompressionListener);
        s.targetFpsProperty().addListener(numberChangeListener);
        s.targetResolutionProperty().addListener(resolutionChangeListener);
      }
      if (old instanceof CameraServerSource) {
        CameraServerSource s = (CameraServerSource) old;
        s.targetCompressionProperty().removeListener(sourceCompressionListener);
        s.targetFpsProperty().removeListener(numberChangeListener);
        s.targetResolutionProperty().removeListener(resolutionChangeListener);
      }
    });

    exportProperties(showControls, showCrosshair, crosshairColor);
  }

  @Override
  public Pane getView() {
    return root;
  }

  @FXML
  private void applySettings() {
    if (getSource() instanceof CameraServerSource) {
      CameraServerSource source = (CameraServerSource) getSource();
      int compression = (int) compressionSlider.getValue();
      int fps = frameRateField.getNumber();
      int width = this.width.getNumber();
      int height = this.height.getNumber();
      boolean change = source.getTargetCompression() != compression
          || source.getTargetFps() != fps
          || source.getTargetResolution().getWidth() != width
          || source.getTargetResolution().getHeight() != height;
      if (!change) {
        return;
      }
      source.setTargetCompression(compression);
      source.setTargetFps(fps);
      if (width > 0 && height > 0) {
        source.setTargetResolution(new Resolution(width, height));
      } else {
        source.setTargetResolution(Resolution.EMPTY);
      }
    }
  }

  public boolean isShowControls() {
    return showControls.get();
  }

  public BooleanProperty showControlsProperty() {
    return showControls;
  }

  public void setShowControls(boolean showControls) {
    this.showControls.set(showControls);
  }

  public boolean isShowCrosshair() {
    return showCrosshair.get();
  }

  public BooleanProperty showCrosshairProperty() {
    return showCrosshair;
  }

  public void setShowCrosshair(boolean showCrosshair) {
    this.showCrosshair.set(showCrosshair);
  }

  public Color getCrosshairColor() {
    return crosshairColor.getValue();
  }

  public Property<Color> crosshairColorProperty() {
    return crosshairColor;
  }

  public void setCrosshairColor(Color crosshairColor) {
    this.crosshairColor.setValue(crosshairColor);
  }
}
