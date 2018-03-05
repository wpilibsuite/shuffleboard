package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.api.data.types.NumberType;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.Time;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import com.google.common.collect.ImmutableList;
import com.sun.prism.GraphicsPipeline;
import com.sun.prism.j2d.J2DPipeline;
import com.sun.prism.sw.SWPipeline;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.util.StringConverter;

@Description(name = "Graph", dataTypes = {Number.class, double[].class})
@ParametrizedController("GraphWidget.fxml")
@SuppressWarnings("PMD.GodClass")
public class GraphWidget implements AnnotatedWidget {

  private static final Logger log = Logger.getLogger(GraphWidget.class.getName());

  static {
    GraphicsPipeline pipeline = GraphicsPipeline.getPipeline();
    System.out.println("Using graphics pipeline: " + pipeline);
    if (pipeline instanceof SWPipeline || pipeline instanceof J2DPipeline) {
      log.warning("Software rendering detected! Graphs will be VERY slow and will most likely make the entire "
          + "application slow down to the point of being completely unusable");
    }
  }

  @FXML
  private Pane root;
  @FXML
  private XYChart<Number, Number> chart;
  @FXML
  private NumberAxis xAxis;
  @FXML
  private NumberAxis yAxis;

  private final StringProperty title = new SimpleStringProperty(this, "title", "");

  private final ObservableList<DataSource> sources = FXCollections.observableArrayList();
  private final Map<DataSource<? extends Number>, XYChart.Series<Number, Number>> numberSeriesMap = new HashMap<>();
  private final Map<DataSource<double[]>, List<XYChart.Series<Number, Number>>> arraySeriesMap = new HashMap<>();
  private final DoubleProperty visibleTime = new SimpleDoubleProperty(this, "Visible time", 30);

  private final Map<XYChart.Series<Number, Number>, BooleanProperty> visibleSeries = new HashMap<>();
  private final Map<XYChart.Series<Number, Number>, ObservableList<XYChart.Data<Number, Number>>> realData
      = new HashMap<>();

  // The number of data points to average
  private static final int SAMPLING_SIZE = 5;

  // How many times each series has been updated in the range [0, SAMPLING_SIZE)
  private final Map<XYChart.Series, Integer> seen = new HashMap<>();

  // The average value of the past N values in each series, where 0 <= N < SAMPLING_SIZE
  // Once the series has been updated SAMPLING_SIZE times, the average value is reset to zero.
  private final Map<XYChart.Series, Double> avg = new HashMap<>();

  private final ChangeListener<Number> numberChangeLister = (property, oldNumber, newNumber) -> {
    final DataSource<Number> source = sourceFor(property);
    updateFromNumberSource(source);
  };

  private final ChangeListener<double[]> numberArrayChangeListener = (property, oldArray, newArray) -> {
    final DataSource<double[]> source = sourceFor(property);
    updateFromArraySource(source);
  };

  private final Function<XYChart.Series<Number, Number>, BooleanProperty> createVisibleProperty = s -> {
    SimpleBooleanProperty visible = new SimpleBooleanProperty(this, s.getName() + " visible", true);
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
      if (getSources().isEmpty()) {
        setTitle("Graph (no sources)");
      } else if (getSources().size() == 1) {
        setTitle(getSources().get(0).getName());
      } else {
        setTitle("Graph (" + getSources().size() + " sources)");
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
          List<XYChart.Data<Number, Number>> toAdd = dataList.stream()
              .filter(d -> d.getXValue().doubleValue() >= xAxis.getLowerBound())
              .filter(d -> d.getXValue().doubleValue() < series.getData().get(0).getXValue().doubleValue())
              .collect(Collectors.toList());
          series.getData().addAll(0, toAdd);
        });
      }
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
    final XYChart.Series<Number, Number> series = getNumberSeries(source);

    // The update HAS TO run on the FX thread, otherwise we run the risk of ConcurrentModificationExceptions
    // when the chart goes to lay out the data
    FxUtils.runOnFxThread(() -> {
      updateSeries(series, now, source.getData().doubleValue());
    });
  }

  private void updateFromArraySource(DataSource<double[]> source) {
    final long now = System.currentTimeMillis();
    final double[] data = source.getData();
    final List<XYChart.Series<Number, Number>> series = getArraySeries(source);

    // The update HAS TO run on the FX thread, otherwise we run the risk of ConcurrentModificationExceptions
    // when the chart goes to lay out the data
    FxUtils.runOnFxThread(() -> {
      for (int i = 0; i < series.size(); i++) {
        updateSeries(series.get(i), now, data[i]);
      }
    });
  }

  private void updateSeries(XYChart.Series<Number, Number> series, long now, double newData) {
    int lastSeen = seen.put(series, (seen.computeIfAbsent(series, __ -> SAMPLING_SIZE - 1) + 1) % SAMPLING_SIZE);
    double currentAvg = (avg.computeIfAbsent(series, __ -> 0.0) + newData) / SAMPLING_SIZE;
    if (lastSeen != 0) {
      avg.put(series, currentAvg);
      return;
    }
    avg.put(series, 0.0);
    long elapsed = now - Time.getStartTime();
    XYChart.Data<Number, Number> point = new XYChart.Data<>(elapsed, currentAvg);
    ObservableList<XYChart.Data<Number, Number>> dataList = series.getData();
    if (!dataList.isEmpty()) {
      // Make the graph a square wave
      // This prevents the graph from appearing to be continuous when the data is discreet
      // Note this only affects the chart; the actual data is not changed
      double prev = dataList.get(dataList.size() - 1).getYValue().doubleValue();
      if (prev != currentAvg) {
        dataList.add(new XYChart.Data<>(elapsed - 1, prev));
      }
    }
    dataList.add(point);
    realData.computeIfAbsent(series, __ -> FXCollections.observableArrayList()).add(point);
    if (!chart.getData().contains(series)
        && Optional.ofNullable(visibleSeries.get(series)).map(Property::getValue).orElse(true)) {
      chart.getData().add(series);
    }
    updateBounds(elapsed);
  }

  private XYChart.Series<Number, Number> getNumberSeries(DataSource<? extends Number> source) {
    if (!numberSeriesMap.containsKey(source)) {
      XYChart.Series<Number, Number> series = new XYChart.Series<>();
      series.setName(source.getName());
      numberSeriesMap.put(source, series);
      realData.put(series, FXCollections.observableArrayList());
      visibleSeries.computeIfAbsent(series, createVisibleProperty);
    }
    return numberSeriesMap.get(source);
  }

  private List<XYChart.Series<Number, Number>> getArraySeries(DataSource<double[]> source) {
    List<XYChart.Series<Number, Number>> series = arraySeriesMap.computeIfAbsent(source, __ -> new ArrayList<>());
    final double[] data = source.getData();
    if (data.length < series.size()) {
      while (series.size() != data.length) {
        XYChart.Series<Number, Number> removed = series.remove(series.size() - 1);
        realData.remove(removed);
        visibleSeries.remove(removed);
      }
    } else if (data.length > series.size()) {
      for (int i = series.size(); i < data.length; i++) {
        XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
        newSeries.setName(source.getName() + "[" + i + "]"); // eg "array[0]", "array[1]", etc
        series.add(newSeries);
        realData.put(newSeries, FXCollections.observableArrayList());
        visibleSeries.computeIfAbsent(newSeries, createVisibleProperty);
      }
    }
    return series;
  }

  private void updateBounds(long elapsed) {
    xAxis.setUpperBound(elapsed);
    removeInvisibleData();
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public Property<String> titleProperty() {
    return title;
  }

  @Override
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (sources.contains(source)) {
      // Already have it, don't graph it twice
      return;
    }
    if (getDataTypes().contains(source.getDataType())) {
      this.sources.add(source);
    } else {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
  }

  @Override
  public ObservableList<DataSource> getSources() {
    return sources;
  }

  @Override
  public List<Property<?>> getProperties() {
    return ImmutableList.<Property<?>>builder()
        .add(visibleTime)
        .addAll(visibleSeries.values()
            .stream()
            .sorted(Comparator.comparing(Property::getName, AlphanumComparator.INSTANCE))
            .collect(Collectors.toList()))
        .build();
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
        XYChart.Data<Number, Number> data = series.getData().get(i);
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

}
