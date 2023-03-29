package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.wpi.first.networktables.NetworkTableEvent.Kind;
import edu.wpi.first.networktables.Topic;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;

import java.util.Optional;

/**
 * A data source backed by a single key-value pair in a network table.
 */
public class SingleKeyNetworkTableSource<T> extends NetworkTableSource<T> {

  /**
   * Flag marking whether or not an update from the table is the first update the source gets. This works around the
   * issue caused when the initial value received from NetworkTables is the same as the default value provided by the
   * data type, which results in the source never being marked active.
   */
  private volatile boolean initialUpdate = true;

  private Optional<String> preferredWidget;

  /**
   * Creates a single-key network table source backed by the value in the given table
   * associated with the given key.
   *
   * @param table    the table backing the source
   * @param key      the key associated with the data
   * @param dataType the allowable data type. A value that is not an instance of this
   *                 type is considered "null" and will make the source inactive
   */
  public SingleKeyNetworkTableSource(NetworkTable table, String key, DataType dataType) {
    super(key, dataType);
    setName(key);
    preferredWidget = loadWidgetFromTopicProps(table.getTopic(key));
    setTableListener((__, event) -> {
      if (event.is(NetworkTableEvent.Kind.kUnpublish)) {
        setActive(false);
      } else if (event.valueData != null) {
        Object value = event.valueData.value.getValue();
        setActive(DataTypes.getDefault().forJavaType(value.getClass()).map(dataType::equals).orElse(false));
        if (!initialUpdate && EqualityUtils.isEqual(value, getData())) {
          // No change
          return;
        }
        initialUpdate = true;


        if (isActive()) {
          setData((T) value);
        }
      } else if (event.is(Kind.kProperties)) {
        preferredWidget = loadWidgetFromTopicProps(table.getTopic(key));
      }
    });

    data.addListener((__, oldValue, newValue) -> {
      if (isUpdateFromNetworkTables()) {
        // The change was from network tables; setting the value again would be redundant
        return;
      }

      NetworkTableEntry entry = table.getEntry(key);
      Object value = entry.getValue().getValue();

      if ((value != null && EqualityUtils.isEqual(value, newValue)) || !isConnected()) {
        // no change
        return;
      }

      if (isActive()) {
        entry.setValue(newValue);
        if (!entry.getTopic().isRetained()) {
          entry.getTopic().setRetained(true);
        }
      } else {
        throw new IllegalStateException("Source is not active");
      }
    });

    Sources.getDefault().register(this);
  }

  @Override
  protected boolean isSingular() {
    return true;
  }

  @Override
  public Optional<String> preferredWidget() {
    return preferredWidget;
  }

  private static Optional<String> loadWidgetFromTopicProps(Topic topic) {
    String metadata = topic.getProperties();
    JsonParser parser = new JsonParser();
    try {
      JsonObject obj = parser.parse(metadata).getAsJsonObject();
      JsonElement widgetProp = obj.get("widget");
      if (widgetProp == null || !widgetProp.isJsonPrimitive()) {
        System.err.println("Metadata widget field for topic `" + topic.getName() + "` doesn't exist or isn't primitive!");
        return Optional.empty();
      }
      return Optional.of(widgetProp.getAsString());
    } catch (Exception e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }
}
