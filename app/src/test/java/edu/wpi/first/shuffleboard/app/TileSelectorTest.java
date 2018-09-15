package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class TileSelectorTest extends ApplicationTest {

  private TileSelector selector;
  private WidgetPane pane;
  private WidgetTile firstTile;
  private WidgetTile secondTile;
  private WidgetTile thirdTile;

  @Override
  public void start(Stage stage) {
    pane = new WidgetPane();
    selector = TileSelector.forPane(pane);
    pane.setNumColumns(3);
    pane.setNumRows(2);
    firstTile = pane.addWidget(new MockWidget());
    secondTile = pane.addWidget(new MockWidget());
    thirdTile = pane.addWidget(new MockWidget());
    stage.setScene(new Scene(pane, 640, 480));
    stage.getScene().getStylesheets().setAll("/edu/wpi/first/shuffleboard/api/base.css");
    stage.show();
  }

  @Test
  public void testInitialConditions() {
    assertAll(
        () -> assertEquals(3, pane.getTiles().size(), "There should be three tiles in the pane"),
        () -> assertEquals(0, selector.getSelectedTiles().size(), "No tiles should be selected"),
        () -> assertFalse(selector.areTilesSelected(), "No tiles should be selected")
    );
  }

  @Test
  public void testSelectSingleTile() {
    Tile<?> tile = firstTile;
    FxUtils.runOnFxThread(() -> selector.select(tile));
    waitForFxEvents();
    assertTrue(tile.isSelected(), "Tile was not selected");
  }

  @Test
  @Tag("NonHeadlessTests")
  public void testDragToSelectSingle() {
    clickOn(pane.localToScreen(128, 256))
        .drag()
        .dropTo(pane.localToScreen(0, 0));
    waitForFxEvents();
    assertTrue(firstTile.isSelected(), "First tile should have been selected by drag");
    assertFalse(secondTile.isSelected(), "Middle tile should not have been selected");
    assertFalse(thirdTile.isSelected(), "Rightmost tile should not have been selected");
  }

  @Test
  public void testDragToSelectMultiple() {
    clickOn(pane.localToScreen(256, 256))
        .drag()
        .dropTo(pane.localToScreen(0, 0));
    waitForFxEvents();
    assertTrue(firstTile.isSelected(), "First tile should have been selected by drag");
    assertTrue(secondTile.isSelected(), "Middle tile should have been selected by drag");
    assertFalse(thirdTile.isSelected(), "Rightmost tile should not have been selected");
  }

  @Test
  public void testDragToMoveMultiple() {
    testDragToSelectMultiple(); // to select the two tiles
    selector.select(firstTile);
    selector.select(secondTile);
    clickOn(firstTile)
        .drag()
        .dropBy(0, 128);
    waitForFxEvents();
    assertEquals(new GridPoint(0, 1), pane.getTileLayout(firstTile).origin);
    assertEquals(new GridPoint(1, 1), pane.getTileLayout(secondTile).origin);
  }

  @Test
  public void testSelectOnly() {
    selector.selectOnly(firstTile);
    assertTrue(firstTile.isSelected(), "First tile should have been selected");
    selector.selectOnly(secondTile);
    assertFalse(firstTile.isSelected(), "First tile should have been deselected");
    assertTrue(secondTile.isSelected(), "Second tile should have been selected");
  }

  @Test
  public void testDoubleClickToSelectUnselectedTile() {
    testDragToSelectMultiple();
    doubleClickOn(thirdTile);
    waitForFxEvents();
    assertAll("All but the first tile should be selected",
        () -> assertFalse(firstTile.isSelected(), "First tile was still selected"),
        () -> assertFalse(secondTile.isSelected(), "Second tile was still selected"),
        () -> assertTrue(thirdTile.isSelected(), "Third tile was not selected")
    );
  }

  @Test
  public void testDoubleClickToSelectTileInSelection() {
    testDragToSelectMultiple();
    doubleClickOn(firstTile);
    assertTrue(firstTile.isSelected(), "First tile should be selected");
    assertFalse(secondTile.isSelected(), "Second tile should have been deselected by double-click");
  }

  @Test
  @Tag("NonHeadlessTests")
  public void testCtrlClickToSelect() {
    press(KeyCode.CONTROL);
    clickOn(firstTile);
    release(KeyCode.CONTROL);
    waitForFxEvents();
    assertTrue(firstTile.isSelected(), "Tile should have been selected by CTRL+click");
  }

  @Test
  @Tag("NonHeadlessTests")
  public void testCtrlClickToAddToSelection() {
    testDragToSelectMultiple();
    press(KeyCode.CONTROL);
    clickOn(thirdTile);
    release(KeyCode.CONTROL);
    waitForFxEvents();
    assertAll("All tiles should be selected",
        () -> assertTrue(firstTile.isSelected(), "First tile was not selected"),
        () -> assertTrue(secondTile.isSelected(), "Second tile was not selected"),
        () -> assertTrue(thirdTile.isSelected(), "Third tile was not selected")
    );
    press(KeyCode.CONTROL);
    clickOn(firstTile);
    release(KeyCode.CONTROL);
    waitForFxEvents();
    assertAll("All but the first tile should be selected",
        () -> assertFalse(firstTile.isSelected(), "First tile was still selected"),
        () -> assertTrue(secondTile.isSelected(), "Second tile was not selected"),
        () -> assertTrue(thirdTile.isSelected(), "Third tile was not selected")
    );
  }

}
