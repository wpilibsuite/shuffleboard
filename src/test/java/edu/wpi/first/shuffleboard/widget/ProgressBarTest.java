package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.sources.DataSource;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
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
    assertEquals(0, widget.progressBar.getProgress());

    source.setData(1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(1, widget.progressBar.getProgress());
  }

  @Test
  public void testRangeChangeAffectsProgress() {
    source.setData(0);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0, widget.progressBar.getProgress());

    widget.minValue.setValue(-1);
    widget.maxValue.setValue(1);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(0.5, widget.progressBar.getProgress());
  }

}
