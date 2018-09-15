package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.widget.Components;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class GridLayoutTest extends ApplicationTest {

  private GridLayout layout;
  private GridPane grid;

  @Override
  public void start(Stage stage) {
    layout = Components.viewFor(GridLayout.class).get();
    grid = (GridPane) layout.getView().getChildren().get(0);
    stage.setScene(new Scene(layout.getView()));
  }

  @Test
  public void testReduceRowCount() {
    FxUtils.runOnFxThread(() -> layout.setNumRows(2));
    waitForFxEvents();
    assertEquals(6, getPlaceholderCount(), "Removing a row should reduce the number of placeholders from 9 to 6");
  }

  @Test
  public void testIncreaseRowCount() {
    FxUtils.runOnFxThread(() -> layout.setNumRows(4));
    waitForFxEvents();
    assertEquals(12, getPlaceholderCount(), "Adding a row should increase the number of placeholders from 9 to 12");
  }

  @Test
  public void testReduceColumnCount() {
    FxUtils.runOnFxThread(() -> layout.setNumColumns(2));
    waitForFxEvents();
    assertEquals(6, getPlaceholderCount(), "Removing a column should reduce the number of placeholders from 9 to 6");
  }

  @Test
  public void testIncreaseColumnCount() {
    FxUtils.runOnFxThread(() -> layout.setNumColumns(4));
    waitForFxEvents();
    assertEquals(12, getPlaceholderCount(), "Adding a column should increase the number of placeholders from 9 to 12");
  }

  private long getPlaceholderCount() {
    return grid
        .getChildren()
        .stream()
        .filter(n -> n instanceof GridLayout.Placeholder)
        .count();
  }

  @Test
  public void testAddNewRowWhenFull() {
    FxUtils.runOnFxThread(() -> {
      layout.setNumColumns(1);
      layout.setNumRows(1);
      layout.addChild(new MockWidget());
    });
    waitForFxEvents();

    assertEquals(1, layout.getNumRows(), "Adding the first child should not modify the row count");
    FxUtils.runOnFxThread(() -> layout.addChild(new MockWidget()));
    waitForFxEvents();
    assertEquals(2, layout.getNumRows(), "Adding a second child with 1 row should have caused a new row to be created");
  }

}
