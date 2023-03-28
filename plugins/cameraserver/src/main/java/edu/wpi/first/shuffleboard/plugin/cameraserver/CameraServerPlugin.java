package edu.wpi.first.shuffleboard.plugin.cameraserver;

import edu.wpi.first.cscore.CameraServerJNI;
import edu.wpi.first.shuffleboard.api.PropertyParser;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.plugin.Requires;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.util.OsDetector;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSourceType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraStreamAdapter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.widget.CameraServerWidget;
import edu.wpi.first.shuffleboard.plugin.cameraserver.widget.CameraServerWidget.Rotation;
import edu.wpi.first.util.CombinedRuntimeLoader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Description(
    group = "edu.wpi.first.shuffleboard",
    name = "CameraServer",
    version = "3.1.1",
    summary = "Provides sources and widgets for viewing CameraServer MJPEG streams"
)
@Requires(group = "edu.wpi.first.shuffleboard", name = "NetworkTables", minVersion = "2.0.0")
public class CameraServerPlugin extends Plugin {

  private static final Logger log = Logger.getLogger(CameraServerPlugin.class.getName());
  private final CameraStreamAdapter streamRecorder = new CameraStreamAdapter();

  private static final PropertyParser<Rotation> CAMERA_ROTATION = PropertyParser.forEnum(Rotation.class);

  @Override
  public void onLoad() throws Exception {
    CameraServerJNI.Helper.setExtractOnStaticLoad(false);
    try {
      // Make sure the JNI is loaded. If it's not, this plugin can't work!
      Loader.load(opencv_java.class);
      var files = CombinedRuntimeLoader.extractLibraries(CameraServerPlugin.class,
          "/ResourceInformation-CameraServer.json");
      CombinedRuntimeLoader.loadLibrary("cscorejnicvstatic", files);
      CameraServerJNI.setTelemetryPeriod(1.0);
    } catch (IOException | UnsatisfiedLinkError ex) {
      log.log(Level.SEVERE, "Failed to load CV Libraries", ex);
      if (OsDetector.isWindows()) {
        log.log(Level.SEVERE, "This failure is likely caused by running an N version of windows."
            + " Camera support will not work");
      }
      throw ex;
    }
  }

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(CameraServerWidget.class)
    );
  }

  @Override
  public Set<PropertyParser<?>> getPropertyParsers() {
    return Set.of(CAMERA_ROTATION);
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of(
        CameraServerDataType.Instance, WidgetType.forAnnotatedWidget(CameraServerWidget.class)
    );
  }

  @Override
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of(
        CameraServerSourceType.INSTANCE
    );
  }

  @Override
  public List<TypeAdapter> getTypeAdapters() {
    return ImmutableList.of(
        streamRecorder
    );
  }

  @Override
  public List<DataType> getDataTypes() {
    return ImmutableList.of(
        CameraServerDataType.Instance
    );
  }

}
