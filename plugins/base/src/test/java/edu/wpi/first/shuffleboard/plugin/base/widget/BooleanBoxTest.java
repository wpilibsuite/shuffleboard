package edu.wpi.first.shuffleboard.plugin.base.widget;


import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BooleanType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BooleanBoxTest extends AbstractWidgetTest {

  private BooleanBox widget;

  @BeforeAll
  public static void register() {
    setRequirements(BooleanBox.class, new BooleanType());
  }

  @Override
  public void start(Stage stage) throws Exception {
    widget = (BooleanBox) Components.getDefault().createWidget("Boolean Box",
        DummySource.forTypes(new BooleanType()).get()).get();
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
