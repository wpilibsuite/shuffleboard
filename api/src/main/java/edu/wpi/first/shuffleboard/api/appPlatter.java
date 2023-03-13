package edu.wpi.first.shuffleboard.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import javafx.application.Platform;
import javafx.scene.Node;

public class appPlatter {

    private static appPlatter instance = null;

    private final Map<String, Consumer<TileSize>> tileSizeListeners = new HashMap<>();
    private final Map<String, Consumer<GridPoint>> tilePosListeners = new HashMap<>();
    private final Map<String, Consumer<String>> tileCompListeners = new HashMap<>();
    private final Map<String, Consumer<Double>> tileOpacListeners = new HashMap<>();
    private final Map<String, Consumer<Boolean>> tileVisListeners = new HashMap<>();

    private Runnable reQueueTiles = () -> {};

    public static appPlatter getInstance() {
        if (instance == null) {
            instance = new appPlatter();
        }
        return instance;
    }

    public void addSizeListener(Node obj, Consumer<TileSize> listener) {
        tileSizeListeners.put(obj.getId(), listener);
    }

    public void addPoseListener(Node obj, Consumer<GridPoint> listener) {
        tilePosListeners.put(obj.getId(), listener);
    }

    public void addCompListener(Node obj, Consumer<String> listener) {
        tileCompListeners.put(obj.getId(), listener);
    }

    public void addOpacityListener(Node obj, Consumer<Double> listener) {
        tileOpacListeners.put(obj.getId(), listener);
    }

    public void addVisibilityListener(Node obj, Consumer<Boolean> listener) {
        tileVisListeners.put(obj.getId(), listener);
    }

    public void removeListener(Node obj) {
        tileSizeListeners.remove(obj.getId());
        tilePosListeners.remove(obj.getId());
        tileCompListeners.remove(obj.getId());
        tileOpacListeners.remove(obj.getId());
        tileVisListeners.remove(obj.getId());
    }

    public void notifySizeListeners(Node obj, TileSize data) {
        refresh();
        var lambda = tileSizeListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
    }

    public void notifyPositionListeners(Node obj, GridPoint data) {
        refresh();
        var lambda = tilePosListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
    }

    public void notifyComponentListeners(Node obj, String data) {
        refresh();
        var lambda = tileCompListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
    }

    public void notifyOpacityListeners(Node obj, double data) {
        refresh();
        var lambda = tileOpacListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
    }

    public void notifyVisibilityListeners(Node obj, boolean data) {
        refresh();
        var lambda = tileVisListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
    }

    public void setReQueueTiles(Runnable reQueueTiles) {
        this.reQueueTiles = reQueueTiles;
    }

    private void refresh() {
        tileSizeListeners.clear();
        tilePosListeners.clear();
        tileCompListeners.clear();
        tileOpacListeners.clear();
        tileVisListeners.clear();
        reQueueTiles.run();
    }
}
