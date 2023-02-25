package edu.wpi.first.shuffleboard.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import javafx.scene.Node;

public class appPlatter {

    private static appPlatter instance = null;

    private final Map<String, Consumer<TileSize>> tileSizeListeners = new HashMap<>();
    private final Map<String, Consumer<GridPoint>> tilePosListeners = new HashMap<>();
    private final Map<String, Consumer<String>> tileCompListeners = new HashMap<>();
    private final Map<String, Consumer<Double>> tileOpacListeners = new HashMap<>();

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

    public void removeListener(Node obj) {
        tileSizeListeners.remove(obj.getId());
        tilePosListeners.remove(obj.getId());
        tileCompListeners.remove(obj.getId());
    }

    public void notifyListeners(Node obj, TileSize data) {
        System.out.println("notifySizeListeners: " + obj.getId());
        var lambda = tileSizeListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
        refresh();
    }

    public void notifyListeners(Node obj, GridPoint data) {
        System.out.println("notifyPoseListeners: " + obj.getId());
        var lambda = tilePosListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
        refresh();
    }

    public void notifyListeners(Node obj, String data) {
        System.out.println("notifyCompListeners: " + obj.getId());
        var lambda = tileCompListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
        refresh();
    }

    public void notifyListeners(Node obj, double data) {
        System.out.println("notifyOpacityListeners: " + obj.getId());
        System.out.println("listeners: " + tileCompListeners.keySet());
        var lambda = tileOpacListeners.get(obj.getId());
        if (lambda != null) {
            lambda.accept(data);
        }
        refresh();
    }

    public void setReQueueTiles(Runnable reQueueTiles) {
        this.reQueueTiles = reQueueTiles;
    }

    private void refresh() {
        tileSizeListeners.clear();
        tilePosListeners.clear();
        tileCompListeners.clear();
        tileOpacListeners.clear();
        reQueueTiles.run();
    }
}
