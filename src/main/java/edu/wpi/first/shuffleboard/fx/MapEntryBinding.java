package edu.wpi.first.shuffleboard.fx;

import edu.wpi.first.shuffleboard.util.FxUtils;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * A binding used to bind to the value of a specific key in a map.
 */
public class MapEntryBinding<K, V> extends ObjectBinding<V> {

  private final ObservableMap<K, V> map;
  private final K key;
  private final ObservableList<?> dependencies;

  /**
   * Creates binding to the value of the given key in the given map.
   *
   * @param map the map to bind to
   * @param key the key associated with the value to bind to
   */
  public MapEntryBinding(ObservableMap<K, V> map, K key) {
    this.map = map;
    this.key = key;
    this.dependencies = FXCollections.singletonObservableList(map);
    map.addListener((MapChangeListener<K, V>) change -> {
      if (change.getKey().equals(this.key)) {
        FxUtils.runOnFxThread(this::invalidate);
      }
    });
  }

  @Override
  protected V computeValue() {
    return map.get(key);
  }

  @Override
  public ObservableList<?> getDependencies() {
    return dependencies;
  }

}
