package edu.wpi.first.shuffleboard.plugin.cameraserver.widget;

import edu.wpi.first.shuffleboard.api.components.IntegerField;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;
import edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization.ImageConverter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSource;

import com.google.common.collect.ImmutableList;
import com.jfoenix.controls.JFXSlider;

import org.fxmisc.easybind.EasyBind;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

@Description(name = "Camera Stream", dataTypes = CameraServerData.class)
@ParametrizedController("CameraServerWidget.fxml")
@SuppressWarnings("PMD.TooManyFields")
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
  private JFXSlider compressionSlider;
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

  private final Mat displayMat = new Mat();
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
    imageView.imageProperty().bind(EasyBind.combine(dataOrDefault, rotation, (data, rotation) -> {
      if (data.getImage() == null) {
        return emptyImage;
      } else {
        data.getImage().copyTo(displayMat);
        rotation.rotate(displayMat);
        return converter.convert(displayMat);
      }
    }));
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
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Crosshair",
            Setting.of("Show crosshair", showCrosshair, Boolean.class),
            Setting.of("Crosshair color", crosshairColor, Color.class)
        ),
        Group.of("Controls",
            Setting.of("Show controls", showControls, Boolean.class),
            Setting.of("Rotation", rotation, Rotation.class)
        )
    );
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
          || source.getTargetResolution().isNotEqual(width, height);
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
    NONE("None", image -> {}),
    QUARTER_CW("90 degrees clockwise", image -> Core.rotate(image, image, Core.ROTATE_90_CLOCKWISE)),
    QUARTER_CCW("90 degrees counter-clockwise", image -> Core.rotate(image, image, Core.ROTATE_90_COUNTERCLOCKWISE)),
    HALF("180 degrees", image -> Core.rotate(image, image, Core.ROTATE_180));

    private final String humanReadable;
    private final RotationStrategy rotationStrategy;

    Rotation(String humanReadable, RotationStrategy rotationStrategy) {
      this.humanReadable = humanReadable;
      this.rotationStrategy = rotationStrategy;
    }

    @Override
    public String toString() {
      return humanReadable;
    }

    void rotate(Mat image) {
      rotationStrategy.rotate(image);
    }
  }

  private interface RotationStrategy {
    void rotate(Mat src);
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
