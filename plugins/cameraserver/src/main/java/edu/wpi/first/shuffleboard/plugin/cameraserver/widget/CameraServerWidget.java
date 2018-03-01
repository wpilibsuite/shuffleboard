package edu.wpi.first.shuffleboard.plugin.cameraserver.widget;

import edu.wpi.first.shuffleboard.api.components.IntegerField;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.properties.SaveThisProperty;
import edu.wpi.first.shuffleboard.api.util.PropertyUtils;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSource;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
  @SavePropertyFrom(propertyName = "min", savedName = "minCompression")
  @SavePropertyFrom(propertyName = "max", savedName = "maxCompression")
  private Slider compressionSlider;
  @FXML
  private IntegerField frameRateField;
  @FXML
  private IntegerField width;
  @FXML
  private IntegerField height;
  @FXML
  private Node crosshairs;

  private final BooleanProperty showControls = new SimpleBooleanProperty(this, "showControls", true);
  private final BooleanProperty showCrosshair = new SimpleBooleanProperty(this, "showCrosshair", true);
  private final Property<Color> crosshairColor = new SimpleObjectProperty<>(this, "crosshairColor", Color.WHITE);

  @SaveThisProperty(name = "resolution")
  private final Property<Resolution> resolution = new SimpleObjectProperty<>(this, "resolution", Resolution.EMPTY);
  @SaveThisProperty(name = "fps")
  private final IntegerProperty fps = new SimpleIntegerProperty(this, "fps");

  @FXML
  private void initialize() {
    imageView.imageProperty().bind(dataOrDefault.map(CameraServerData::getImage).orElse(emptyImage));
    PropertyUtils.bindBidirectionalWithConverter(frameRateField.numberProperty(), fps, i -> i, Number::intValue);
    width.numberProperty().addListener((__, old, width) -> {
      resolution.setValue(new Resolution(width, resolution.getValue().getHeight()));
    });
    height.numberProperty().addListener((__, old, height) -> {
      resolution.setValue(new Resolution(resolution.getValue().getWidth(), height));
    });
    resolution.addListener((__, old, resolution) -> {
      width.setNumber(resolution.getWidth());
      height.setNumber(resolution.getHeight());
    });

    sourceProperty().addListener((__, old, source) -> {
      if (source instanceof CameraServerSource) {
        bindToSource((CameraServerSource) source);
      }
      if (old instanceof CameraServerSource) {
        unbindFromSource((CameraServerSource) old);
      }
    });

    exportProperties(showControls, showCrosshair, crosshairColor);
  }

  private void bindToSource(CameraServerSource source) {
    compressionSlider.setValue(source.getTargetCompression());
    fps.set(source.getTargetFps());
    resolution.setValue(source.getTargetResolution());
    source.targetCompressionProperty().bindBidirectional(compressionSlider.valueProperty());
    source.targetFpsProperty().bindBidirectional(fps);
    source.targetResolutionProperty().bindBidirectional(resolution);
  }

  private void unbindFromSource(CameraServerSource source) {
    source.targetCompressionProperty().unbindBidirectional(compressionSlider.valueProperty());
    source.targetFpsProperty().unbindBidirectional(fps);
    source.targetResolutionProperty().unbindBidirectional(resolution);
    source.setTargetCompression(-1);
    source.setTargetFps(-1);
    source.setTargetResolution(Resolution.EMPTY);
  }

  @Override
  public Pane getView() {
    return root;
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
