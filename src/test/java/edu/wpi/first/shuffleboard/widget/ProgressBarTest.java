package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.sources.DataSource;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.Assert.*;

public class ProgressBarTest extends ApplicationTest {

  private ProgressBar widget;
  private DataSource<Number> source;

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
    widget = (ProgressBar) Widgets.createWidget(
        "Progress Bar", DummySource.forTypes(DataTypes.Number).get()).get();
    source = widget.getSource();
    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @Test
  public void testProgress() {
    source.setData(0);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0, widget.progressBar.getProgress(), 0);

    source.setData(1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(1, widget.progressBar.getProgress(), 0);
  }

  @Test
  public void testRangeChangeAffectsProgress() {
    source.setData(0);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0, widget.progressBar.getProgress(), 0);

    widget.minValue.setValue(-1);
    widget.maxValue.setValue(1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0.5, widget.progressBar.getProgress(), 0);
  }

  @Test
  public void testAllowIndeterminateValueNegativeTrue() {
    widget.minValue.setValue(0);
    widget.maxValue.setValue(1);
    widget.allowIndeterminateValue.setValue(true);
    source.setData(-0.5);

    assertTrue(widget.progressBar.isIndeterminate());
  }

  @Test
  public void testAllowIndeterminateValueNegativeFalse() {
    widget.minValue.setValue(0);
    widget.maxValue.setValue(1);
    widget.allowIndeterminateValue.setValue(false);
    source.setData(-0.5);

    assertFalse(widget.progressBar.isIndeterminate());
  }

  @Test
  public void testAllowIndeterminateValuePositiveTrue() {
    widget.minValue.setValue(0);
    widget.maxValue.setValue(1);
    widget.allowIndeterminateValue.setValue(true);
    source.setData(1.5);

    assertTrue(widget.progressBar.isIndeterminate());
  }

  @Test
  public void testAllowIndeterminateValuePositiveFalse() {
    widget.minValue.setValue(0);
    widget.maxValue.setValue(1);
    widget.allowIndeterminateValue.setValue(false);
    source.setData(1.5);

    assertFalse(widget.progressBar.isIndeterminate());
  }

}
