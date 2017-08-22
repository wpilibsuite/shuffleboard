package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.widget.Widgets;
import edu.wpi.first.shuffleboard.plugin.base.data.types.NumberType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProgressBarTest extends AbstractWidgetTest {

  private ProgressBar widget;
  private DataSource<Number> source;

  @BeforeAll
  public static void register() {
    setRequirements(ProgressBar.class, new NumberType());
  }

  @Override
  public void start(Stage stage) throws Exception {
    widget = (ProgressBar) Widgets.getDefault().createWidget(
        "Progress Bar", DummySource.forTypes(new NumberType()).get()).get();
    source = widget.getSource();
    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @Test
  public void testProgress() {
    source.setData(-1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0, widget.progressBar.getProgress());

    source.setData(1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(1, widget.progressBar.getProgress());
  }

  @Test
  public void testRangeChangeAffectsProgress() {
    source.setData(0);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0.5, widget.progressBar.getProgress());

    widget.minValue.setValue(0);
    widget.maxValue.setValue(1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0.0, widget.progressBar.getProgress());
  }

}
