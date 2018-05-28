package edu.wpi.first.shuffleboard.plugin.cameraserver.widget;

import edu.wpi.first.shuffleboard.api.components.IntegerField;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;
import edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization.ImageConverter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSource;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
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
  @FXML
  private Label fpsLabel;
  @FXML
  private Label bandwidthLabel;
  @FXML
  private Pane imageContainer;
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

  private final ImageConverter converter = new ImageConverter();

  private final BooleanProperty showControls = new SimpleBooleanProperty(this, "showControls", true);
  private final BooleanProperty showCrosshair = new SimpleBooleanProperty(this, "showCrosshair", true);
  private final Property<Color> crosshairColor = new SimpleObjectProperty<>(this, "crosshairColor", Color.WHITE);
  private final Property<Rotation> rotation = new SimpleObjectProperty<>(this, "rotation", Rotation.NONE);
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
    imageView.imageProperty().bind(dataOrDefault
        .map(CameraServerData::getImage)
        .map(converter::convert)
        .orElse(emptyImage));
    fpsLabel.textProperty().bind(dataOrDefault.map(CameraServerData::getFps).map(fps -> {
      if (fps < 0) {
        return "--- FPS";
      } else {
        return String.format("%.2f FPS", fps);
      }
    }));
    bandwidthLabel.textProperty().bind(dataOrDefault.map(CameraServerData::getBandwidth).map(bandwidth -> {
      if (bandwidth < 0) {
        return "--- Mbps";
      } else {
        double mbps = bandwidth * 8 / 1e6;
        return String.format("%.2f Mbps", mbps);
      }
    }));
    width.setMaxValue(CameraServerSource.MAX_RESOLUTION.getWidth());
    height.setMaxValue(CameraServerSource.MAX_RESOLUTION.getHeight());

    sourceProperty().addListener((__, old, source) -> {
      if (source instanceof CameraServerSource) {
        CameraServerSource newSource = (CameraServerSource) source;
        if (source.hasClients()) {
          compressionSlider.setValue(newSource.getTargetCompression());
          frameRateField.setNumber(newSource.getTargetFps());
          width.setNumber(newSource.getTargetResolution().getWidth());
          height.setNumber(newSource.getTargetResolution().getHeight());
        } else {
          applySettings();
        }
        newSource.targetCompressionProperty().addListener(sourceCompressionListener);
        newSource.targetFpsProperty().addListener(numberChangeListener);
        newSource.targetResolutionProperty().addListener(resolutionChangeListener);
      }
      if (old instanceof CameraServerSource) {
        CameraServerSource oldSource = (CameraServerSource) old;
        oldSource.targetCompressionProperty().removeListener(sourceCompressionListener);
        oldSource.targetFpsProperty().removeListener(numberChangeListener);
        oldSource.targetResolutionProperty().removeListener(resolutionChangeListener);
      }
    });
    rotation.addListener((__, old, rotation) -> {
      imageContainer.setRotate(rotation.degrees());
      if (((rotation == Rotation.QUARTER_CW) && (old == Rotation.QUARTER_CCW))
          || ((rotation == Rotation.QUARTER_CCW) && (old == Rotation.QUARTER_CW))
          || ((rotation == Rotation.HALF) && (old == Rotation.NONE))
          || ((rotation == Rotation.NONE) && (old == Rotation.HALF))) {
        // No change
        return;
      }
      if (rotation.degrees() % 180 == 0) {
        // Reset
        imageContainer.setMaxWidth(-1);
        imageContainer.setMaxHeight(-1);
      } else {
        // Constrain width and height to avoid overflowing widget bounds
        imageContainer.setMaxWidth(imageContainer.getHeight());
        imageContainer.setMaxHeight(imageContainer.getWidth());
      }
    });

    exportProperties(rotation, showControls, showCrosshair, crosshairColor);
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

  public enum Rotation {
    NONE("None", 0),
    QUARTER_CW("90 degrees clockwise", 270),
    QUARTER_CCW("90 degrees counter-clockwise", 90),
    HALF("180 degrees", 180);

    private final String humanReadable;
    private final double degrees;

    Rotation(String humanReadable, double degrees) {
      this.humanReadable = humanReadable;
      this.degrees = degrees;
    }

    @Override
    public String toString() {
      return humanReadable;
    }

    public double degrees() {
      return degrees;
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
