package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataTypes;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import static org.junit.Assert.assertEquals;

public class BooleanBoxTest extends ApplicationTest {

  private BooleanBox widget;

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
    widget = (BooleanBox) Widgets.createWidget("Boolean Box",
        DummySource.forTypes(DataTypes.Boolean).get()).get();
    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @Test
  public void testColorWhenTrue() {
    widget.getSource().setData(true);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("Background should be the 'true' color", widget.getTrueColor(), getBackground());
  }

  @Test
  public void testColorWhenFalse() {
    widget.getSource().setData(false);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("Background should be the 'false' color", widget.getFalseColor(), getBackground());
  }

  @Test
  public void testChangeTrueColor() {
    final Color color = Color.WHITE;
    widget.getSource().setData(true);
    widget.setTrueColor(color);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("Background was the wrong color", color, getBackground());
  }

  @Test
  public void testChangeFalseColor() {
    widget.getSource().setData(false);
    widget.setFalseColor(Color.BLACK);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("Background was the wrong color", widget.getFalseColor(), getBackground());
  }

  private Paint getBackground() {
    return widget.getView().getBackground().getFills().get(0).getFill();
  }

}
