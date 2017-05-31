package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.NetworkTableEntry;
import edu.wpi.first.shuffleboard.NetworkTableRequired;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.*;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class NetworkTableTreeTest extends ApplicationTest {

  private NetworkTable table;
  private NetworkTableTree tree;
  private TreeItem<NetworkTableEntry> root;

  @Override
  public void start(Stage stage) throws Exception {
    NetworkTableRequired.setUpNetworkTables();
    table = NetworkTable.getTable("");
    tree = new NetworkTableTree();
    tree.getKeyColumn().prefWidthProperty().bind(tree.widthProperty().divide(2));
    tree.getValueColumn().prefWidthProperty().bind(tree.widthProperty().divide(2));
    root = tree.getRoot();
    tree.setShowRoot(false);
    stage.setScene(new Scene(tree));
    stage.show();
  }

  @After
  public void tearDown() {
    NetworkTableRequired.tearDownNetworkTables();
  }

  @Test
  public void testEmpty() {
    assertTrue("There should be no children", root.getChildren().isEmpty());
  }

  @Test
  public void testFirstLevel() {
    table.putString("entry", "value");
    waitForFxEvents();

    ObservableList<TreeItem<NetworkTableEntry>> children = root.getChildren();
    TreeItem<NetworkTableEntry> child = children.get(0);
    NetworkTableEntry entry = child.getValue();
    assertEquals("/entry", entry.getKey());
    assertEquals("entry", entry.simpleKey());
    assertEquals("value", entry.getDisplayString());
    assertTrue("Child should be a leaf", child.isLeaf());
  }

  @Test
  public void testBranches() {
    table.putString("branch/entry", "x");
    waitForFxEvents();
    ObservableList<TreeItem<NetworkTableEntry>> children = root.getChildren();

    final TreeItem<NetworkTableEntry> branch = children.get(0);
    final TreeItem<NetworkTableEntry> leaf = branch.getChildren().get(0);

    assertEquals("/branch", branch.getValue().getKey());
    assertEquals("", branch.getValue().getDisplayString());
    assertFalse("Branch should not be a leaf", branch.isLeaf());
    assertTrue("Branch should be expanded", branch.isExpanded());

    assertEquals("/branch/entry", leaf.getValue().getKey());
    assertEquals("entry", leaf.getValue().simpleKey());
    assertEquals("x", leaf.getValue().getDisplayString());
    assertTrue("Value node was not a leaf", leaf.isLeaf());
  }

  @Test
  public void testSort() {
    // deliberately not in order
    table.putString("c", "");
    table.putString("sub_a/sub_entry_a", "");
    table.putString("a", "");
    table.putString("b", "");
    table.putString("sub_b/sub_entry_b", "");
    waitForFxEvents();
    tree.sort();

    ObservableList<TreeItem<NetworkTableEntry>> children = root.getChildren();
    assertEquals("There should be 5 children", 5, children.size());
    assertEquals("/sub_a", children.get(0).getValue().getKey());
    assertEquals("/sub_b", children.get(1).getValue().getKey());
    assertEquals("/a", children.get(2).getValue().getKey());
    assertEquals("/b", children.get(3).getValue().getKey());
    assertEquals("/c", children.get(4).getValue().getKey());

    assertCellIndex("sub_a", 0);
    assertCellIndex("sub_entry_a", 1);
    assertCellIndex("sub_b", 2);
    assertCellIndex("sub_entry_b", 3);
    assertCellIndex("a", 4);
    assertCellIndex("b", 5);
    assertCellIndex("c", 6);
  }

  private void assertCellIndex(String simpleKey, int index) {
    TreeTableCell cell = lookup(hasText(simpleKey)).query();
    assertNotNull("No cell with text " + simpleKey, cell);
    assertTrue("Cell is not visible", cell.isVisible());
    assertEquals("Wrong index", index, cell.getTreeTableRow().getIndex());
  }

  @Test
  public void testDelete() {
    String key = "testDelete";
    table.putString(key, "value");
    waitForFxEvents();

    assertNotNull("There should be a cell for the entry", lookup(hasText(key)).query());

    table.delete(key);
    waitForFxEvents();
    assertNull("The cell should have been removed", lookup(hasText(key)).query());
  }

  @Test
  public void testUpdate() {
    final String key = "testUpdate";
    final String firstValue = "value 1";
    final String secondValue = "value 2";
    table.putString(key, firstValue);
    waitForFxEvents();
    assertCellIndex(key, 0);
    assertCellIndex(firstValue, 0);
    assertNotNull(lookup(hasText(firstValue)).query());
    assertNull(lookup(hasText(secondValue)).query());

    table.putString(key, secondValue);
    waitForFxEvents();
    assertCellIndex(key, 0);
    assertNull(lookup(hasText(firstValue)).query());
    assertCellIndex(secondValue, 0);
  }

}