package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;
import edu.wpi.first.shuffleboard.app.plugin.PluginObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class DeserializationHelper {
  private DeserializationHelper() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  /**
   * A workaround for DragBoard serialization not using Plugin classloaders.
   * @param object an object returned by the dragboard
   * @return a SourceEntry, either the object casted or deserialized
   */
  public static SourceEntry sourceFromDrag(Object object) {
    if (object instanceof ByteBuffer) {
      ByteBuffer buffer = (ByteBuffer) object;

      try {
        ByteArrayInputStream is = new ByteArrayInputStream(buffer.array());
        PluginObjectInputStream pois = new PluginObjectInputStream(PluginLoader.getDefault().getClassLoaders(), is);
        return (SourceEntry) pois.readObject();
      } catch (IOException | ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    } else {
      return (SourceEntry) object;
    }
  }
}
