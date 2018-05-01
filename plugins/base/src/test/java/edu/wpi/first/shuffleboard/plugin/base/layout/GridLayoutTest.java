package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.plugin.base.widget.TextViewWidget;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

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
    layout.setNumRows(2);
    waitForFxEvents();
    assertEquals(6, getPlaceholderCount(), "Removing a row should reduce the number of placeholders from 9 to 6");
  }

  @Test
  public void testIncreaseRowCount() {
    layout.setNumRows(4);
    waitForFxEvents();
    assertEquals(12, getPlaceholderCount(), "Adding a row should increase the number of placeholders from 9 to 12");
  }

  @Test
  public void testReduceColumnCount() {
    layout.setNumColumns(2);
    waitForFxEvents();
    assertEquals(6, getPlaceholderCount(), "Removing a column should reduce the number of placeholders from 9 to 6");
  }

  @Test
  public void testIncreaseColumnCount() {
    layout.setNumColumns(4);
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
    layout.setNumColumns(1);
    layout.setNumRows(1);
    Widget widget = new MockWidget();
    layout.addChild(widget);

    assertEquals(1, layout.getNumRows(), "Adding the first child should not modify the row count");
    layout.addChild(new MockWidget());
    assertEquals(2, layout.getNumRows(), "Adding a second child with 1 row should have caused a new row to be created");
  }

}
