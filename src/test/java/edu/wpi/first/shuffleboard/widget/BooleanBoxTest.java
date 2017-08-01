package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataTypes;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
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
    assertEquals(widget.getTrueColor(), getBackground(), "Background should be the 'true' color");
  }

  @Test
  public void testColorWhenFalse() {
    widget.getSource().setData(false);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(widget.getFalseColor(), getBackground(), "Background should be the 'false' color");
  }

  @Test
  public void testChangeTrueColor() {
    final Color color = Color.WHITE;
    widget.getSource().setData(true);
    widget.setTrueColor(color);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(color, getBackground(), "Background was the wrong color");
  }

  @Test
  public void testChangeFalseColor() {
    widget.getSource().setData(false);
    widget.setFalseColor(Color.BLACK);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(widget.getFalseColor(), getBackground(), "Background was the wrong color");
  }

  private Paint getBackground() {
    return widget.getView().getBackground().getFills().get(0).getFill();
  }

}
