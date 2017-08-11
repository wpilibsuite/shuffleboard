package edu.wpi.first.shuffleboard.api.components;


import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceTreeTableTest extends ApplicationTest {

  private SourceType sourceType = new SourceType("Test", false, "test://", __ -> null);
  private SourceTreeTable<SourceEntry, ?> tree;
  private TreeItem<SourceEntry> root = new TreeItem<>(sourceType.createRootSourceEntry());

  @Override
  public void start(Stage stage) throws Exception {
    tree = new SourceTreeTable<>();
    tree.setRoot(root);
    tree.setShowRoot(true);
    tree.setSourceType(sourceType);
    stage.setScene(new Scene(new StackPane(tree)));
    stage.show();
  }

  @Test
  public void testUpdateFirstLevelEntry() {
    SourceEntry e = sourceType.createSourceEntryForUri("firstLevel");
    tree.updateEntry(e);
    ObservableList<TreeItem<SourceEntry>> children = root.getChildren();
    assertEquals(1, children.size(), "Should be 1 child");
    TreeItem<SourceEntry> child = children.get(0);
    assertAll("", () -> {
      assertEquals("firstLevel", child.getValue().getName());
    });
  }

}
