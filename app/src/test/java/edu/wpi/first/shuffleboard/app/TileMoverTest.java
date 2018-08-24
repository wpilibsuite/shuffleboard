package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class TileMoverTest extends ApplicationTest {

  private static final TileSize ONE_BY_ONE = new TileSize(1, 1);
  private static final TileSize TWO_BY_ONE = new TileSize(2, 1);
  private static final TileSize ONE_BY_TWO = new TileSize(1, 2);
  private WidgetPane pane;

  @Override
  public void start(Stage stage) {
    pane = new WidgetPane();
  }

  @Test
  public void testMoveLeftToFillGaps() {
    // ||1|_|2|_|3|| -> ||1|2|3||
    // given
    pane.setNumColumns(5);
    Tile<?> first = new WidgetTile(new MockWidget(), ONE_BY_ONE);
    Tile<?> second = new WidgetTile(new MockWidget(), ONE_BY_ONE);
    Tile<?> third = new WidgetTile(new MockWidget(), ONE_BY_ONE);
    pane.addTile(first, new GridPoint(0, 0), ONE_BY_ONE);
    pane.addTile(second, new GridPoint(2, 0), ONE_BY_ONE);
    pane.addTile(third, new GridPoint(4, 0), ONE_BY_ONE);
    waitForFxEvents();

    // when
    pane.setNumColumns(3);
    waitForFxEvents();

    // then
    assertAll(
        () -> assertEquals(new GridPoint(0, 0), pane.getTileLayout(first).origin),
        () -> assertEquals(new GridPoint(1, 0), pane.getTileLayout(second).origin),
        () -> assertEquals(new GridPoint(2, 0), pane.getTileLayout(third).origin)
    );
  }

  @Test
  public void testMoveUpToFillGaps() {
    // given
    pane.setNumRows(5);
    Tile<?> first = new WidgetTile(new MockWidget(), ONE_BY_ONE);
    Tile<?> second = new WidgetTile(new MockWidget(), ONE_BY_ONE);
    Tile<?> third = new WidgetTile(new MockWidget(), ONE_BY_ONE);
    pane.addTile(first, new GridPoint(0, 0), ONE_BY_ONE);
    pane.addTile(second, new GridPoint(0, 2), ONE_BY_ONE);
    pane.addTile(third, new GridPoint(0, 4), ONE_BY_ONE);
    waitForFxEvents();

    // when
    pane.setNumRows(3);
    waitForFxEvents();

    // then
    assertAll(
        () -> assertEquals(new GridPoint(0, 0), pane.getTileLayout(first).origin),
        () -> assertEquals(new GridPoint(0, 1), pane.getTileLayout(second).origin),
        () -> assertEquals(new GridPoint(0, 2), pane.getTileLayout(third).origin)
    );
  }

  @Test
  public void testShrinkLeft() {
    // ||1|1|2|2|| -> ||1|2||
    // given
    pane.setNumColumns(4);
    Tile<?> first = new WidgetTile(new MockWidget(), TWO_BY_ONE);
    Tile<?> second = new WidgetTile(new MockWidget(), TWO_BY_ONE);
    pane.addTile(first, new GridPoint(0, 0), TWO_BY_ONE);
    pane.addTile(second, new GridPoint(2, 0), TWO_BY_ONE);
    waitForFxEvents();

    // when
    pane.setNumColumns(2);
    waitForFxEvents();

    // then
    assertAll(
        () -> assertEquals(new GridPoint(0, 0), pane.getTileLayout(first).origin),
        () -> assertEquals(ONE_BY_ONE, pane.getTileLayout(first).size),
        () -> assertEquals(new GridPoint(1, 0), pane.getTileLayout(second).origin),
        () -> assertEquals(ONE_BY_ONE, pane.getTileLayout(second).size)
    );
  }

  @Test
  public void testShrinkUp() {
    // given
    pane.setNumRows(4);
    Tile<?> first = new WidgetTile(new MockWidget(), ONE_BY_TWO);
    Tile<?> second = new WidgetTile(new MockWidget(), ONE_BY_TWO);
    pane.addTile(first, new GridPoint(0, 0), ONE_BY_TWO);
    pane.addTile(second, new GridPoint(0, 2), ONE_BY_TWO);
    waitForFxEvents();

    // when
    pane.setNumRows(2);
    waitForFxEvents();

    // then
    assertAll(
        () -> assertEquals(new GridPoint(0, 0), pane.getTileLayout(first).origin),
        () -> assertEquals(ONE_BY_ONE, pane.getTileLayout(first).size),
        () -> assertEquals(new GridPoint(0, 1), pane.getTileLayout(second).origin),
        () -> assertEquals(ONE_BY_ONE, pane.getTileLayout(second).size)
    );
  }

  @Test
  public void testShrinkAndMoveLeft() {
    // ||1|1|_|2|2|| -> ||1|1|2||
    // given
    pane.setNumColumns(5);
    Tile<?> first = new WidgetTile(new MockWidget(), TWO_BY_ONE);
    Tile<?> second = new WidgetTile(new MockWidget(), TWO_BY_ONE);
    pane.addTile(first, new GridPoint(0, 0), TWO_BY_ONE);
    pane.addTile(second, new GridPoint(3, 0), TWO_BY_ONE);
    waitForFxEvents();

    // when
    pane.setNumColumns(3);
    waitForFxEvents();

    // then
    assertAll(
        () -> assertEquals(new GridPoint(0, 0), pane.getTileLayout(first).origin),
        () -> assertEquals(TWO_BY_ONE, pane.getTileLayout(first).size),
        () -> assertEquals(new GridPoint(2, 0), pane.getTileLayout(second).origin),
        () -> assertEquals(ONE_BY_ONE, pane.getTileLayout(second).size)
    );
  }

  @Test
  public void testShrinkAndMoveUp() {
    // given
    pane.setNumRows(5);
    Tile<?> first = new WidgetTile(new MockWidget(), ONE_BY_TWO);
    Tile<?> second = new WidgetTile(new MockWidget(), ONE_BY_TWO);
    pane.addTile(first, new GridPoint(0, 0), ONE_BY_TWO);
    pane.addTile(second, new GridPoint(0, 3), ONE_BY_TWO);
    waitForFxEvents();

    // when
    pane.setNumRows(3);
    waitForFxEvents();

    // then
    assertAll(
        () -> assertEquals(new GridPoint(0, 0), pane.getTileLayout(first).origin),
        () -> assertEquals(ONE_BY_TWO, pane.getTileLayout(first).size),
        () -> assertEquals(new GridPoint(0, 2), pane.getTileLayout(second).origin),
        () -> assertEquals(ONE_BY_ONE, pane.getTileLayout(second).size)
    );
  }

}
