package edu.wpi.first.shuffleboard.plugin.cameraserver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization.CameraServerDataSerializer;
import edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraServerSourceType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.widget.CameraServerWidget;

import org.opencv.core.Core;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CameraServerPlugin extends Plugin {

  private static final Logger log = Logger.getLogger(CameraServerPlugin.class.getName());

  @SuppressWarnings("JavadocMethod")
  public CameraServerPlugin() {
    super("edu.wpi.first.shuffleboard",
        "CameraServer",
        "1.0.0",
        "Provides sources and widgets for viewing CameraServer MJPEG streams.");
  }

  @Override
  public void onLoad() {
    log.info("OpenCV version: " + Core.VERSION);
    // Make sure the JNI is loaded. If it's not, this plugin can't work!
    // Calling a function from CameraServerJNI will extract the OpenCV JNI dependencies and load them
    CameraServerJNI.getHostname();
  }

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(CameraServerWidget.class)
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of(
        CameraServerDataType.INSTANCE, WidgetType.forAnnotatedWidget(CameraServerWidget.class)
    );
  }

  @Override
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of(
        CameraServerSourceType.INSTANCE
    );
  }

  @Override
  public List<DataType> getDataTypes() {
    return ImmutableList.of(
        CameraServerDataType.INSTANCE
    );
  }

  @Override
  public List<TypeAdapter> getTypeAdapters() {
    return ImmutableList.of(
        new CameraServerDataSerializer()
    );
  }

}
