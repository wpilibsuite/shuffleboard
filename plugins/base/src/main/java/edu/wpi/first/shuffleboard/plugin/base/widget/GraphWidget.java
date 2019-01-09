package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.api.data.types.NumberType;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.api.util.Time;
import edu.wpi.first.shuffleboard.api.widget.AbstractWidget;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

@Description(name = "Graph", dataTypes = {Number.class, double[].class})
@ParametrizedController("GraphWidget.fxml")
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyFields", "PMD.ExcessiveMethodLength"})
public class GraphWidget extends AbstractWidget implements AnnotatedWidget {

  @FXML
  private Pane root;
  @FXML
  private XYChart<Number, Number> chart;
  @FXML
  private NumberAxis xAxis;
  @FXML
  private NumberAxis yAxis;

  private final Map<DataSource<? extends Number>, Series<Number, Number>> numberSeriesMap = new HashMap<>();
  private final Map<DataSource<double[]>, List<Series<Number, Number>>> arraySeriesMap = new HashMap<>();
  private final DoubleProperty visibleTime = new SimpleDoubleProperty(this, "Visible time", 30);

  private final Map<Series<Number, Number>, BooleanProperty> visibleSeries = new HashMap<>();

  private final Object queueLock = new Object();
  private final Map<Series<Number, Number>, List<Data<Number, Number>>> queuedData = new HashMap<>();

  private final ChangeListener<Number> numberChangeLister = (property, oldNumber, newNumber) -> {
    final DataSource<Number> source = sourceFor(property);
    updateFromNumberSource(source);
  };

  private final ChangeListener<double[]> numberArrayChangeListener = (property, oldArray, newArray) -> {
    final DataSource<double[]> source = sourceFor(property);
    updateFromArraySource(source);
  };

  private final Map<Series<Number, Number>, SimpleData> realData = new HashMap<>();

  private final Function<Series<Number, Number>, BooleanProperty> createVisibleProperty = s -> {
    SimpleBooleanProperty visible = new SimpleBooleanProperty(this, s.getName(), true);
    visible.addListener((__, was, is) -> {
      if (is) {
        if (!chart.getData().contains(s)) {
          chart.getData().add(s);
        }
      } else {
        chart.getData().remove(s);
      }
    });
    return visible;
  };

  /**
   * Keep track of all graph widgets so they update at the same time.
   * It's jarring to see a bunch of graphs all updating at different times
   */
  private static final Collection<GraphWidget> graphWidgets =
      Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

  /**
   * How often graphs should be redrawn, in milliseconds.
   */
  private static final long UPDATE_PERIOD = 250;

  static {
    ThreadUtils.newDaemonScheduledExecutorService()
        .scheduleAtFixedRate(() -> {
          synchronized (graphWidgets) {
            graphWidgets.forEach(GraphWidget::update);
          }
        }, 500, UPDATE_PERIOD, TimeUnit.MILLISECONDS);
  }

  @FXML
  private void initialize() {
    chart.legendVisibleProperty().bind(
        Bindings.createBooleanBinding(() -> sources.size() > 1, sources));
    sources.addListener((ListChangeListener<DataSource>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(source -> {
            if (source.getDataType() == NumberType.Instance) {
              source.dataProperty().addListener(numberChangeLister);
              if (source.isConnected()) {
                numberChangeLister.changed(source.dataProperty(), null, (Number) source.getData());
              }
            } else if (source.getDataType() == NumberArrayType.Instance) {
              source.dataProperty().addListener(numberArrayChangeListener);
              if (source.isConnected()) {
                numberArrayChangeListener.changed(source.dataProperty(), null, (double[]) source.getData());
              }
            } else {
              throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
            }
          });
        } else if (c.wasRemoved()) {
          c.getRemoved().forEach(source -> {
            source.dataProperty().removeListener(numberChangeLister);
            source.dataProperty().removeListener(numberArrayChangeListener);
          });
        }
      }
    });
    xAxis.setTickLabelFormatter(new StringConverter<Number>() {
      @Override
      public String toString(Number num) {
        final int seconds = num.intValue() / 1000;
        return toString(Math.abs(seconds), seconds < 0);
      }

      private String toString(int seconds, boolean negative) {
        return String.format("%s%d:%02d", negative ? "-" : "", (seconds / 60) % 60, seconds % 60);
      }

      @Override
      public Number fromString(String string) {
        throw new UnsupportedOperationException("This shouldn't be called");
      }
    });

    xAxis.lowerBoundProperty().bind(xAxis.upperBoundProperty().subtract(visibleTime.multiply(1e3)));

    // Make sure data gets re-added to the chart
    visibleTime.addListener((__, prev, cur) -> {
      if (cur.doubleValue() > prev.doubleValue()) {
        // insert data at the beginning of each series
        realData.forEach((series, dataList) -> {
          List<Data<Number, Number>> toAdd = new ArrayList<>();
          for (int i = 0; i < dataList.getXValues().size(); i++) {
            double x = dataList.getXValues().get(i);
            if (x >= xAxis.getLowerBound()) {
              if (x < series.getData().get(0).getXValue().doubleValue()) {
                Data<Number, Number> data = dataList.asData(i);
                if (!toAdd.isEmpty()) {
                  Data<Number, Number> squarifier = new Data<>(
                      data.getXValue().doubleValue() - 1,
                      toAdd.get(toAdd.size() - 1).getYValue().doubleValue()
                  );
                  toAdd.add(squarifier);
                }
                toAdd.add(data);
              } else {
                break;
              }
            }
          }
          series.getData().addAll(0, toAdd);
        });
      }
    });

    ActionList.registerSupplier(root, () -> {
      return ActionList.withName(getTitle())
          .addAction("Clear", () -> {
            synchronized (queueLock) {
              chart.getData().forEach(s -> s.getData().clear());
              queuedData.forEach((s, q) -> q.clear());
              realData.forEach((s, d) -> d.clear());
            }
          });
    });

    // Add this widget to the list only after everything is initialized to prevent occasional null pointers when
    // the update thread runs after construction but before FXML injection or initialization
    synchronized (graphWidgets) {
      graphWidgets.add(this);
    }

  }

  @SuppressWarnings("unchecked")
  private <T> DataSource<T> sourceFor(ObservableValue<? extends T> property) {
    // Check the bean - if it's a DataSource's data property, the bean should be the source itself
    if (property instanceof Property) {
      Object bean = ((Property) property).getBean();
      if (bean instanceof DataSource) {
        return (DataSource<T>) bean;
      }
    }
    // Fallback to search the sources for the property
    return sources.stream()
        .filter(source -> source.dataProperty() == property)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("No source for " + property));
  }

  private void updateFromNumberSource(DataSource<? extends Number> source) {
    final long now = Time.now();
    final Series<Number, Number> series = getNumberSeries(source);

    // The update HAS TO run on the FX thread, otherwise we run the risk of ConcurrentModificationExceptions
    // when the chart goes to lay out the data
    FxUtils.runOnFxThread(() -> {
      updateSeries(series, now, source.getData().doubleValue());
    });
  }

  private void updateFromArraySource(DataSource<double[]> source) {
    final long now = System.currentTimeMillis();
    final double[] data = source.getData();
    final List<Series<Number, Number>> series = getArraySeries(source);

    // The update HAS TO run on the FX thread, otherwise we run the risk of ConcurrentModificationExceptions
    // when the chart goes to lay out the data
    FxUtils.runOnFxThread(() -> {
      for (int i = 0; i < series.size(); i++) {
        updateSeries(series.get(i), now, data[i]);
      }
    });
  }

  private void updateSeries(Series<Number, Number> series, long now, double newData) {
    final long elapsed = now - Time.getStartTime();
    final Data<Number, Number> point = new Data<>(elapsed, newData);
    final ObservableList<Data<Number, Number>> dataList = series.getData();
    Data<Number, Number> squarifier = null;
    realData.computeIfAbsent(series, __ -> new SimpleData()).add(point);
    synchronized (queueLock) {
      List<Data<Number, Number>> queue = queuedData.computeIfAbsent(series, __ -> new ArrayList<>());
      if (queue.isEmpty()) {
        if (!dataList.isEmpty()) {
          squarifier = createSquarifier(newData, elapsed, dataList);
        }
      } else {
        squarifier = createSquarifier(newData, elapsed, queue);
      }
      if (squarifier != null) {
        queue.add(squarifier);
      }
      queue.add(point);
    }
    if (!chart.getData().contains(series)
        && Optional.ofNullable(visibleSeries.get(series)).map(Property::getValue).orElse(true)) {
      chart.getData().add(series);
    }
  }

  private Data<Number, Number> createSquarifier(double newData, long elapsed, List<Data<Number, Number>> queue) {
    // Make the graph a square wave
    // This prevents the graph from appearing to be continuous when the data is discreet
    // Note this only affects the chart; the actual data is not changed
    Data<Number, Number> squarifier = null;
    double prev = queue.get(queue.size() - 1).getYValue().doubleValue();
    if (prev != newData) {
      squarifier = new Data<>(elapsed - 1, prev);
    }
    return squarifier;
  }

  private Series<Number, Number> getNumberSeries(DataSource<? extends Number> source) {
    if (!numberSeriesMap.containsKey(source)) {
      Series<Number, Number> series = new Series<>();
      series.setName(source.getName());
      numberSeriesMap.put(source, series);
      realData.put(series, new SimpleData());
      visibleSeries.computeIfAbsent(series, createVisibleProperty);
      series.nodeProperty().addListener((__, old, node) -> {
        if (node instanceof Parent) {
          Parent parent = (Parent) node;
          parent.getChildrenUnmodifiable().forEach(child -> {
            child.setCache(true);
            child.setCacheHint(CacheHint.SPEED);
          });
          parent.setCache(true);
          parent.setCacheHint(CacheHint.SPEED);
        }
      });
    }
    return numberSeriesMap.get(source);
  }

  private List<Series<Number, Number>> getArraySeries(DataSource<double[]> source) {
    List<Series<Number, Number>> series = arraySeriesMap.computeIfAbsent(source, __ -> new ArrayList<>());
    final double[] data = source.getData();
    if (data.length < series.size()) {
      while (series.size() != data.length) {
        Series<Number, Number> removed = series.remove(series.size() - 1);
        realData.remove(removed);
        visibleSeries.remove(removed);
      }
    } else if (data.length > series.size()) {
      for (int i = series.size(); i < data.length; i++) {
        Series<Number, Number> newSeries = new Series<>();
        newSeries.setName(source.getName() + "[" + i + "]"); // eg "array[0]", "array[1]", etc
        series.add(newSeries);
        realData.put(newSeries, new SimpleData());
        visibleSeries.computeIfAbsent(newSeries, createVisibleProperty);
      }
    }
    return series;
  }

  private void updateBounds(long elapsed) {
    xAxis.setUpperBound(elapsed);
    removeInvisibleData();
  }

  private void update() {
    FxUtils.runOnFxThread(() -> {
      if (chart.getData().isEmpty()) {
        return;
      }
      synchronized (queueLock) {
        queuedData.forEach((series, queuedData) -> series.getData().addAll(queuedData));
        queuedData.forEach((series, queuedData) -> queuedData.clear());
        OptionalLong maxX = chart.getData().stream()
            .map(Series::getData)
            .filter(d -> !d.isEmpty())
            .map(d -> d.get(d.size() - 1))
            .map(Data::getXValue)
            .mapToLong(Number::longValue)
            .max();
        if (maxX.isPresent()) {
          updateBounds(maxX.getAsLong());
        }
      }
    });
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (sources.contains(source)) {
      // Already have it, don't graph it twice
      return;
    }
    super.addSource(source);
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Graph",
            Setting.of("Visible time", visibleTime, Double.class)
        ),
        Group.of("Visible data",
            visibleSeries.values()
                .stream()
                .sorted(Comparator.comparing(Property::getName, AlphanumComparator.INSTANCE))
                .map(p -> Setting.of(p.getName(), p, Boolean.class))
                .collect(Collectors.toList())
        )
    );
  }

  public double getVisibleTime() {
    return visibleTime.get();
  }

  public long getVisibleTimeMs() {
    return (long) (getVisibleTime() * 1000);
  }

  public DoubleProperty visibleTimeProperty() {
    return visibleTime;
  }

  public void setVisibleTime(double visibleTime) {
    this.visibleTime.set(visibleTime);
  }

  /**
   * Removes data from the data series that is outside the visible chart area to improve performance.
   */
  private void removeInvisibleData() {
    final double lower = xAxis.getLowerBound();
    realData.forEach((series, dataList) -> {
      int firstBeforeOutOfRange = -1;
      for (int i = 0; i < series.getData().size(); i++) {
        Data<Number, Number> data = series.getData().get(i);
        if (data.getXValue().doubleValue() >= lower) {
          firstBeforeOutOfRange = i;
          break;
        }
      }
      if (firstBeforeOutOfRange > 0) {
        series.getData().remove(0, firstBeforeOutOfRange);
      }
    });
  }

  /**
   * Stores data in two parallel arrays.
   */
  private static final class SimpleData {
    private final PrimitiveDoubleArrayList xValues = new PrimitiveDoubleArrayList();
    private final PrimitiveDoubleArrayList yValues = new PrimitiveDoubleArrayList();

    public void add(double x, double y) {
      xValues.add(x);
      yValues.add(y);
    }

    public void add(Data<? extends Number, ? extends Number> point) {
      add(point.getXValue().doubleValue(), point.getYValue().doubleValue());
    }

    public PrimitiveDoubleArrayList getXValues() {
      return xValues;
    }

    public PrimitiveDoubleArrayList getYValues() {
      return yValues;
    }

    public Data<Number, Number> asData(int index) {
      return new Data<>(xValues.get(index), yValues.get(index));
    }

    public void clear() {
      xValues.clear();
      yValues.clear();
    }
  }

}
