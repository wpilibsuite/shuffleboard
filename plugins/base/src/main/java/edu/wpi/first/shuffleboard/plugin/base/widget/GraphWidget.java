package edu.wpi.first.shuffleboard.plugin.base.widget;

import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.Time;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
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
@SuppressWarnings("PMD.GodClass") // uh oh
public class GraphWidget implements AnnotatedWidget {

  @FXML
  private Pane root;
  @FXML
  private XYChart<Number, Number> chart;
  @FXML
  private NumberAxis xAxis;
  @FXML
  private NumberAxis yAxis;

  private final ObservableList<DataSource> sources = FXCollections.observableArrayList();
  private final Map<DataSource<? extends Number>, XYChart.Series<Number, Number>> numberSeriesMap = new HashMap<>();
  private final Map<DataSource<double[]>, List<XYChart.Series<Number, Number>>> arraySeriesMap = new HashMap<>();
  private final DoubleProperty visibleTime = new SimpleDoubleProperty(this, "Visible time", 30);

  private final Map<XYChart.Series<Number, Number>, ObservableList<XYChart.Data<Number, Number>>> realData
      = new HashMap<>();

  private final ChangeListener<? extends Number> numberChangeLister = (property, oldNumber, newNumber) -> {
    final DataSource<Number> source = sourceFor(property);
    updateFromNumberSource(source);
  };

  private final ChangeListener<double[]> numberArrayChangeListener = (property, oldArray, newArray) -> {
    final DataSource<double[]> source = sourceFor(property);
    updateFromArraySource(source);
  };

  @FXML
  private void initialize() {
    chart.legendVisibleProperty().bind(
        Bindings.createBooleanBinding(() -> chart.getData().size() > 1, chart.getData()));
    sources.addListener((ListChangeListener<DataSource>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(source -> {
            if (source.getData() instanceof Number) {
              source.dataProperty().addListener(numberChangeLister);
            } else if (source.getData() instanceof double[]) {
              source.dataProperty().addListener(numberArrayChangeListener);
            } else {
              throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
            }
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
    long elapsed = now - Time.getStartTime();
    XYChart.Data<Number, Number> point = new XYChart.Data<>(elapsed, newData);
    realData.computeIfAbsent(series, __ -> FXCollections.observableArrayList());
    ObservableList<XYChart.Data<Number, Number>> dataList = series.getData();
    if (!dataList.isEmpty()) {
      // Make the graph a square wave
      // This prevents the graph from appearing to be continuous when the data is discreet
      // Note this only affects the chart; the actual data is not changed
      double prev = dataList.get(dataList.size() - 1).getYValue().doubleValue();
      if (prev != newData) {
        dataList.add(new XYChart.Data<>(elapsed - 1, prev));
      }
    }
    dataList.add(point);
    if (!chart.getData().contains(series)) {
      chart.getData().add(series);
    }
    updateBounds(elapsed);
  }

  private XYChart.Series<Number, Number> getNumberSeries(DataSource<? extends Number> source) {
    if (!numberSeriesMap.containsKey(source)) {
      XYChart.Series<Number, Number> series = new XYChart.Series<>();
      series.setName(source.getName());
      numberSeriesMap.put(source, series);
    }
    return numberSeriesMap.get(source);
  }

  private List<XYChart.Series<Number, Number>> getArraySeries(DataSource<double[]> source) {
    List<XYChart.Series<Number, Number>> series = arraySeriesMap.computeIfAbsent(source, __ -> new ArrayList<>());
    final double[] data = source.getData();
    if (data.length < series.size()) {
      while (series.size() != data.length) {
        series.remove(series.size() - 1);
      }
    } else if (data.length > series.size()) {
      for (int i = series.size(); i < data.length; i++) {
        XYChart.Series<Number, Number> newSeries = new XYChart.Series<>();
        newSeries.setName(source.getName() + "[" + i + "]"); // eg "array[0]", "array[1]", etc
        series.add(newSeries);
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
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (sources.contains(source) || sources.stream().anyMatch(s -> s.getName().equals(source.getName()))) {
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
    return ImmutableList.of(
        visibleTime
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
      series.getData().removeIf(d -> d.getXValue().doubleValue() < lower);
    });
  }

}