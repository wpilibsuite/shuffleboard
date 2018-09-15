package edu.wpi.first.shuffleboard.plugin.networktables.components;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.shuffleboard.api.components.SourceTreeTable;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTableTreeWidget;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceEntry;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.stage.Stage;

import static edu.wpi.first.shuffleboard.plugin.networktables.components.NetworkTableTreeItemMatcher.hasDisplayString;
import static edu.wpi.first.shuffleboard.plugin.networktables.components.NetworkTableTreeItemMatcher.hasKey;
import static edu.wpi.first.shuffleboard.plugin.networktables.components.NetworkTableTreeItemMatcher.hasSimpleKey;
import static edu.wpi.first.shuffleboard.plugin.networktables.components.NetworkTableTreeItemMatcher.isExpanded;
import static edu.wpi.first.shuffleboard.plugin.networktables.components.NetworkTableTreeItemMatcher.isLeaf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.util.NodeQueryUtils.hasText;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Disabled("Everything is broken")
@Tag("UI")
public class NetworkTableTreeTest extends ApplicationTest {

  private NetworkTable table;
  private SourceTreeTable<NetworkTableSourceEntry, ?> tree;
  private TreeItem<NetworkTableSourceEntry> root;

  @Override
  public void start(Stage stage) throws Exception {
    NetworkTableUtils.shutdown();
    table = NetworkTableInstance.getDefault().getTable("");
    NetworkTableTreeWidget widget = new NetworkTableTreeWidget();
    widget.setSource(NetworkTableSource.forKey(""));
    tree = widget.getTree();
    root = tree.getRoot();
    tree.setShowRoot(false);
    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @AfterEach
  public void tearDown() {
    NetworkTableUtils.shutdown();
  }

  @Test
  public void testEmpty() {
    assertTrue(root.getChildren().isEmpty(), "There should be no children");
  }

  @Test
  public void testFirstLevel() {
    table.getEntry("entry").setString("value");
    NetworkTableInstance.getDefault().waitForEntryListenerQueue(-1.0);
    waitForFxEvents();

    ObservableList<TreeItem<NetworkTableSourceEntry>> children = root.getChildren();
    assertEquals(1, children.size(), "There should be a single child");
    TreeItem<NetworkTableSourceEntry> child = children.get(0);
    assertThat(child, hasKey("/entry"));
    assertThat(child, hasSimpleKey("entry"));
    assertThat(child, hasDisplayString("value"));
    assertThat("Child should be a leaf", child, isLeaf());
  }

  @Test
  public void testBranches() {
    table.getEntry("branch/entry").setString("x");
    NetworkTableInstance.getDefault().waitForEntryListenerQueue(-1.0);
    waitForFxEvents();
    ObservableList<TreeItem<NetworkTableSourceEntry>> children = root.getChildren();

    assertEquals(1, children.size(), "There should be 1 first-level child");
    assertEquals(1, children.get(0).getChildren().size(), "There should be 1 second-level child");
    final TreeItem<NetworkTableSourceEntry> branch = children.get(0);
    final TreeItem<NetworkTableSourceEntry> leaf = branch.getChildren().get(0);

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
    table.getEntry("c").setString("");
    table.getEntry("sub_a/sub_entry_a").setString("");
    table.getEntry("a").setString("");
    table.getEntry("b").setString("");
    table.getEntry("sub_b/sub_entry_b").setString("");
    NetworkTableInstance.getDefault().waitForEntryListenerQueue(-1.0);
    waitForFxEvents();
    tree.sort();

    ObservableList<TreeItem<NetworkTableSourceEntry>> children = root.getChildren();
    assertEquals(5, children.size(), "There should be 5 children");
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
    assertNotNull(cell, "No cell with text " + simpleKey);
    assertTrue(cell.isVisible(), "Cell is not visible");
    assertEquals(index, cell.getTreeTableRow().getIndex(), "Wrong index");
  }

  @Test
  public void testDelete() {
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();

    String key = "testDelete";
    table.getEntry(key).setString("value");
    inst.waitForEntryListenerQueue(-1.0);
    waitForFxEvents();

    assertNotNull(lookup(hasText(key)).query(), "There should be a cell for the entry");

    table.delete(key);
    inst.waitForEntryListenerQueue(-1.0);
    waitForFxEvents();
    assertNull(lookup(hasText(key)).query(), "The cell should have been removed");
  }

  @Test
  public void testUpdate() {
    final String key = "testUpdate";
    final String firstValue = "value 1";
    final String secondValue = "value 2";
    final NetworkTableInstance inst = NetworkTableInstance.getDefault();

    table.getEntry(key).setString(firstValue);
    inst.waitForEntryListenerQueue(-1.0);
    waitForFxEvents();
    assertCellIndex(key, 0);
    assertCellIndex(firstValue, 0);
    assertNotNull(lookup(hasText(firstValue)).query());
    assertNull(lookup(hasText(secondValue)).query());

    table.getEntry(key).setString(secondValue);
    inst.waitForEntryListenerQueue(-1.0);
    waitForFxEvents();
    assertCellIndex(key, 0);
    assertNull(lookup(hasText(firstValue)).query());
    assertCellIndex(secondValue, 0);
  }

}
