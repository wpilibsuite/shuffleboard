package edu.wpi.first.shuffleboard.fx;

import edu.wpi.first.shuffleboard.util.FxUtils;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 *
 */
public class MapEntryBinding<K, V> extends ObjectBinding<V> {

  private final ObservableMap<K, V> map;
  private final K key;
  private final ObservableList<?> dependencies;

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
