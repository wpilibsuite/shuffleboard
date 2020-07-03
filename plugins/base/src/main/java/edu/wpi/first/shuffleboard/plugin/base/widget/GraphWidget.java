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

import de.gsi.chart.XYChart;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;


@Description(name = "Graph", dataTypes = {Number.class, double[].class})
@ParametrizedController("GraphWidget.fxml")
@SuppressWarnings({"PMD.GodClass", "PMD.TooManyFields", "PMD.ExcessiveMethodLength"})
public class GraphWidget extends AbstractWidget implements AnnotatedWidget {
  @FXML
  private Pane root;
  @FXML
  private XYChart chart;
  @FXML
  private DefaultNumericAxis xAxis;
  @FXML
  private DefaultNumericAxis yAxis;

  private final BooleanProperty xAxisAutoScrolling = new SimpleBooleanProperty(true);
  private final BooleanProperty yAxisAutoRanging = new SimpleBooleanProperty(true);
  private final DoubleProperty yAxisMinBound = new SimpleDoubleProperty(-1);
  private final DoubleProperty yAxisMaxBound = new SimpleDoubleProperty(1);
  private final StringProperty yAxisUnit = new SimpleStringProperty("ul");
  private final DoubleProperty visibleTime = new SimpleDoubleProperty(30);

  private final Map<DataSource<? extends Number>, DoubleDataSet> numberSeriesMap = new HashMap<>();
  private final Map<DataSource<double[]>, List<DoubleDataSet>> arraySeriesMap = new HashMap<>();

  private final Map<DoubleDataSet, BooleanProperty> visibleSeries = new IdentityHashMap<>();

  private final ChangeListener<Number> numberChangeListener = (property, oldNumber, newNumber) -> {
    final DataSource<Number> source = sourceFor(property);
    updateFromNumberSource(source);
  };

  private final ChangeListener<double[]> numberArrayChangeListener = (property, oldArray, newArray) -> {
    final DataSource<double[]> source = sourceFor(property);
    updateFromArraySource(source);
  };

  private final Function<DoubleDataSet, BooleanProperty> createVisibleProperty = s -> {
    SimpleBooleanProperty visible = new SimpleBooleanProperty(this, s.getName(), true);
    visible.addListener((__, was, is) -> {
      if (is) {
        if (!chart.getDatasets().contains(s)) {
          chart.getDatasets().add(s);
        }
      } else {
        chart.getDatasets().remove(s);
      }
    });
    return visible;
  };

  /**
   * Keep track of all graph widgets so they update at the same time.
   * It's jarring to see a bunch of graphs all updating at different times
   *
   */
  private static final Collection<GraphWidget> graphWidgets =
      Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

  public static class Updater implements AutoCloseable {

    private final IntegerProperty graphUpdateRate = new SimpleIntegerProperty(this, "graphUpdateRate", 10);
    private final ScheduledExecutorService executorService = ThreadUtils.newDaemonScheduledExecutorService();
    private volatile ScheduledFuture<?> currentFuture;

    private final ChangeListener<Number> updateCreator = (observable, oldValue, newValue) -> {
      if (currentFuture != null) {
        currentFuture.cancel(false);
      }

      long amount = 1000L / newValue.intValue();
      if (amount == 0) {
        amount = 1;
      }

      currentFuture = executorService
              .scheduleAtFixedRate(this::updateAll, 500, amount, TimeUnit.MILLISECONDS);
    };

    public Updater() {
      graphUpdateRate.addListener(updateCreator);
      updateCreator.changed(null, null, graphUpdateRate.get());
    }

    private void updateAll() {
      graphWidgets.forEach(GraphWidget::update);
    }

    public int getGraphUpdateRate() {
      return graphUpdateRate.get();
    }

    public IntegerProperty graphUpdateRateProperty() {
      return graphUpdateRate;
    }

    public void setGraphUpdateRate(int graphUpdateRate) {
      this.graphUpdateRate.set(graphUpdateRate);
    }

    @Override
    public void close() {
      graphUpdateRate.removeListener(updateCreator);
      executorService.shutdown();
    }
  }

  @FXML
  private void initialize() {
    chart.setAutoNotification(false);
    yAxis.unitProperty().bind(yAxisUnit);

    yAxisAutoRanging.addListener((__, was, useAutoRanging) -> {
      if (useAutoRanging) {
        yAxis.minProperty().unbind();
        yAxis.maxProperty().unbind();

        yAxis.setAutoRanging(true);
      } else {
        yAxis.setAutoRanging(false);
        yAxis.minProperty().bind(yAxisMinBound);
        yAxis.maxProperty().bind(yAxisMaxBound);
      }
    });

    chart.legendVisibleProperty().bind(
        Bindings.createBooleanBinding(() -> sources.size() > 1, sources));
    sources.addListener((ListChangeListener<DataSource>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(source -> {
            if (source.getDataType() == NumberType.Instance) {
              source.dataProperty().addListener(numberChangeListener);
              if (source.isConnected()) {
                numberChangeListener.changed(source.dataProperty(), null, (Number) source.getData());
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
            source.dataProperty().removeListener(numberChangeListener);
            source.dataProperty().removeListener(numberArrayChangeListener);
          });
        }
      }
    });

    xAxis.setTickLabelFormatter(new StringConverter<>() {
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

    ActionList.registerSupplier(root, () ->
        ActionList.withName(getTitle())
        .addAction("Clear", this::clear));

    ActionList.registerSupplier(root, () ->
        ActionList.withName(getTitle())
            .addAction("Toggle X-Axis Autoscroll",
                () -> this.xAxisAutoScrolling.set(!this.xAxisAutoScrolling.get())));


    // Add this widget to the list only after everything is initialized to prevent occasional null pointers when
    // the update thread runs after construction but before FXML injection or initialization
    synchronized (graphWidgets) {
      graphWidgets.add(this);
    }
  }

  private void clear() {
    chart.getDatasets().forEach(s -> {
      var doubleDataSet = (DoubleDataSet) s;
      doubleDataSet.lock().writeLockGuard(
          doubleDataSet::clearData
      );
    });
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
    final DoubleDataSet series = getNumberSeries(source);

    // The update HAS TO run on the FX thread, otherwise we run the risk of ConcurrentModificationExceptions
    // when the chart goes to lay out the data
    FxUtils.runOnFxThread(() -> updateSeries(series, now, source.getData().doubleValue()));
  }

  private void updateFromArraySource(DataSource<double[]> source) {
    final long now = System.currentTimeMillis();
    final double[] data = source.getData();
    final List<DoubleDataSet> series = getArraySeries(source);

    // The update HAS TO run on the FX thread, otherwise we run the risk of ConcurrentModificationExceptions
    // when the chart goes to lay out the data
    FxUtils.runOnFxThread(() -> {
      for (int i = 0; i < series.size(); i++) {
        updateSeries(series.get(i), now, data[i]);
      }
    });
  }

  private void updateSeries(DoubleDataSet data, long now, double nextValue) {
    final long elapsed = now - Time.getStartTime();


    data.lock().writeLockGuard(() -> {
      // So getValues() does not return an array of all the elements, but instead returns the
      // backing array of the ArrayList?? This means it can be followed by trailing zeros,
      // hence the call to getDataCount().

      // This code here makes the graph  square wave and prevents discrete points
      // from appearing continuous.
      double[] yValues = data.getValues(DataSet.DIM_Y);
      if (data.getDataCount(DataSet.DIM_Y) > 1 && yValues[yValues.length - 1] != nextValue) {
        data.add(elapsed - 1, yValues[data.getDataCount(DataSet.DIM_Y) - 1]);
      }

      data.add(elapsed, nextValue);
    });

    boolean dataVisible = Optional.ofNullable(visibleSeries.get(data)).map(Property::getValue).orElseThrow();

    // if listeners do not work (initial value mainly)
    if (!chart.getDatasets().contains(data) && dataVisible) {
      chart.getDatasets().add(data);
    }

    if (chart.getDatasets().contains(data) && !dataVisible) {
      chart.getDatasets().remove(data);
    }
  }

  private DoubleDataSet getNumberSeries(DataSource<? extends Number> source) {
    if (!numberSeriesMap.containsKey(source)) {
      DoubleDataSet series = new DoubleDataSet(source.getName());
      numberSeriesMap.put(source, series);
      visibleSeries.computeIfAbsent(series, createVisibleProperty);
    }
    return numberSeriesMap.get(source);
  }

  private List<DoubleDataSet> getArraySeries(DataSource<double[]> source) {
    List<DoubleDataSet> series = arraySeriesMap.computeIfAbsent(source, __ -> new ArrayList<>());
    final double[] data = source.getData();
    if (data.length < series.size()) {
      while (series.size() != data.length) {
        DoubleDataSet removed = series.remove(series.size() - 1);
        visibleSeries.remove(removed);
      }
    } else if (data.length > series.size()) {
      for (int i = series.size(); i < data.length; i++) {
        DoubleDataSet newSeries = new DoubleDataSet(source.getName() + "[" + i + "]");
        series.add(newSeries);
        visibleSeries.computeIfAbsent(newSeries, createVisibleProperty);
      }
    }
    return series;
  }

  private void update() {
    FxUtils.runOnFxThread(() -> {
      // Data is only pushed to the graph via listeners, so this prevents the graph
      // from staying still during a period of no updates.
      for (var source : numberSeriesMap.keySet()) {
        numberChangeListener.changed(source.dataProperty(), null, source.getData());
      }

      for (var source : arraySeriesMap.keySet()) {
        numberArrayChangeListener.changed(source.dataProperty(), null, source.getData());
      }

      rerenderGraph();
    });
  }

  private void rerenderGraph() {
    OptionalDouble globalMax = OptionalDouble.empty();
    for (DataSet s : chart.getDatasets()) {
      var doubleDataSet = (DoubleDataSet) s;

      OptionalDouble dataSetMax = doubleDataSet.lock().readLockGuard(() -> {

        if (doubleDataSet.getDataCount(DataSet.DIM_X) == 0) {
          return OptionalDouble.empty();
        }

        double[] xValues = doubleDataSet.getValues(DataSet.DIM_X);
        return OptionalDouble.of(xValues[doubleDataSet.getDataCount(DataSet.DIM_X) - 1]);
      });

      if (dataSetMax.isPresent()) {
        if (globalMax.isPresent() && dataSetMax.getAsDouble() > globalMax.getAsDouble()) {
          globalMax = dataSetMax;
        } else if (globalMax.isEmpty()) {
          globalMax = dataSetMax;
        }
      }

      doubleDataSet.fireInvalidated(null);
    }

    if (xAxisAutoScrolling.get() && globalMax.isPresent()) {
      xAxis.maxProperty().set(globalMax.getAsDouble());
      xAxis.minProperty().bind(xAxis.maxProperty().subtract(visibleTime.multiply(1e3)));
    } else {
      xAxis.maxProperty().unbind();
      xAxis.minProperty().unbind();
    }
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
            Setting.of("Visible time", visibleTime, Double.class),
            Setting.of("X-axis auto scrolling", "Automatically scroll the x-axis", xAxisAutoScrolling, Boolean.class)
        ),
        // Note: users can set the lower bound to be greater than the upper bound, resulting in an upside-down graph
        Group.of("Y-axis",
            Setting.of(
                "Automatic bounds",
                "Automatically determine upper and lower bounds based on the visible data",
                yAxisAutoRanging,
                Boolean.class
            ),
            Setting.of(
                "Upper bound",
                "Force a maximum value. Requires 'Automatic bounds' to be disabled",
                yAxisMaxBound,
                Double.class
            ),
            Setting.of(
                "Lower bound",
                "Force a minimum value. Requires 'Automatic bounds' to be disabled",
                yAxisMinBound,
                Double.class
            ),
            Setting.of(
                "Unit",
                "The unit displayed on the y-axis",
                yAxisUnit,
                String.class
            )
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
}
