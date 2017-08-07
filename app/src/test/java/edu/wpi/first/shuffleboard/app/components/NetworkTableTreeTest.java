package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.app.NetworkTableEntry;
import edu.wpi.first.shuffleboard.api.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.app.widget.NetworkTableTreeWidget;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;

import static edu.wpi.first.shuffleboard.api.util.NetworkTableUtils.waitForNtcoreEvents;
import static edu.wpi.first.shuffleboard.app.components.NetworkTableTreeItemMatcher.hasDisplayString;
import static edu.wpi.first.shuffleboard.app.components.NetworkTableTreeItemMatcher.hasKey;
import static edu.wpi.first.shuffleboard.app.components.NetworkTableTreeItemMatcher.hasSimpleKey;
import static edu.wpi.first.shuffleboard.app.components.NetworkTableTreeItemMatcher.isExpanded;
import static edu.wpi.first.shuffleboard.app.components.NetworkTableTreeItemMatcher.isLeaf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.testfx.matcher.base.NodeMatchers.hasText;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

public class NetworkTableTreeTest extends ApplicationTest {

  private NetworkTable table;
  private TreeTableView<NetworkTableEntry> tree;
  private TreeItem<NetworkTableEntry> root;

  @Override
  public void start(Stage stage) throws Exception {
    NetworkTableUtils.shutdown();
    table = NetworkTable.getTable("");
    NetworkTableTreeWidget widget = new NetworkTableTreeWidget();
    widget.setSource(NetworkTableSource.forKey(""));
    tree = widget.getTree();
    root = tree.getRoot();
    tree.setShowRoot(false);
    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @After
  public void tearDown() {
    NetworkTableUtils.shutdown();
  }

  @Test
  public void testEmpty() {
    assertTrue("There should be no children", root.getChildren().isEmpty());
  }

  @Test
  public void testFirstLevel() {
    table.putString("entry", "value");
    waitForNtcoreEvents();
    waitForFxEvents();

    ObservableList<TreeItem<NetworkTableEntry>> children = root.getChildren();
    TreeItem<NetworkTableEntry> child = children.get(0);
    assertThat(child, hasKey("/entry"));
    assertThat(child, hasSimpleKey("entry"));
    assertThat(child, hasDisplayString("value"));
    assertThat("Child should be a leaf", child, isLeaf());
  }

  @Test
  public void testBranches() {
    table.putString("branch/entry", "x");
    waitForNtcoreEvents();
    waitForFxEvents();
    ObservableList<TreeItem<NetworkTableEntry>> children = root.getChildren();

    final TreeItem<NetworkTableEntry> branch = children.get(0);
    final TreeItem<NetworkTableEntry> leaf = branch.getChildren().get(0);

    assertThat(branch, hasKey("/branch"));
    assertThat(branch, hasDisplayString(""));
    assertThat("Branch should not be a leaf", branch, not(isLeaf()));
    assertThat("Branch should be expanded", branch, isExpanded());

    assertThat(leaf, hasKey("/branch/entry"));
    assertThat(leaf, hasSimpleKey("entry"));
    assertThat(leaf, hasDisplayString("x"));
    assertThat(leaf, isLeaf());
  }

  @Test
  public void testSort() {
    // deliberately not in order
    table.putString("c", "");
    table.putString("sub_a/sub_entry_a", "");
    table.putString("a", "");
    table.putString("b", "");
    table.putString("sub_b/sub_entry_b", "");
    waitForNtcoreEvents();
    waitForFxEvents();
    tree.sort();

    ObservableList<TreeItem<NetworkTableEntry>> children = root.getChildren();
    assertEquals("There should be 5 children", 5, children.size());
    assertThat(root, hasKey("/sub_a").atIndex(0));
    assertThat(root, hasKey("/sub_b").atIndex(1));
    assertThat(root, hasKey("/a").atIndex(2));
    assertThat(root, hasKey("/b").atIndex(3));
    assertThat(root, hasKey("/c").atIndex(4));

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
    waitForNtcoreEvents();
    waitForFxEvents();

    assertNotNull("There should be a cell for the entry", lookup(hasText(key)).query());

    table.delete(key);
    waitForNtcoreEvents();
    waitForFxEvents();
    assertNull("The cell should have been removed", lookup(hasText(key)).query());
  }

  @Test
  public void testUpdate() {
    final String key = "testUpdate";
    final String firstValue = "value 1";
    final String secondValue = "value 2";
    table.putString(key, firstValue);
    waitForNtcoreEvents();
    waitForFxEvents();
    assertCellIndex(key, 0);
    assertCellIndex(firstValue, 0);
    assertNotNull(lookup(hasText(firstValue)).query());
    assertNull(lookup(hasText(secondValue)).query());

    table.putString(key, secondValue);
    waitForNtcoreEvents();
    waitForFxEvents();
    assertCellIndex(key, 0);
    assertNull(lookup(hasText(firstValue)).query());
    assertCellIndex(secondValue, 0);
  }

}