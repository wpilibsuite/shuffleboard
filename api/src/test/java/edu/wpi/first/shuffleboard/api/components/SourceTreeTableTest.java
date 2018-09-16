package edu.wpi.first.shuffleboard.api.components;


import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("UI")
public class SourceTreeTableTest extends ApplicationTest {

  private final SourceType sourceType = new MockSourceType();
  private final TreeItem<SourceEntry> root = new FilterableTreeItem<>(sourceType.createRootSourceEntry());
  private SourceTreeTable<SourceEntry, ?> tree;

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
    SourceEntry entry = sourceType.createSourceEntryForUri("firstLevel");
    tree.updateEntry(entry);
    ObservableList<TreeItem<SourceEntry>> children = root.getChildren();
    assertEquals(1, children.size(), "Should be 1 child");
    TreeItem<SourceEntry> child = children.get(0);
    assertAll("", () -> {
      assertEquals("firstLevel", child.getValue().getName());
    });
  }

  private static class MockSourceType extends SourceType {

    public MockSourceType() {
      super("Test", false, "test://", __ -> null);
    }

    @Override
    public DataType<?> dataTypeForSource(DataTypes registry, String sourceUri) {
      return DataTypes.Unknown;
    }

    @Override
    public SourceEntry createSourceEntryForUri(String uri) {
      return new MockSourceEntry(uri);
    }

  }

  private static class MockSourceEntry implements SourceEntry {

    private final String uri;

    public MockSourceEntry(String uri) {
      this.uri = uri;
    }

    @Override
    public String getName() {
      return uri;
    }

    @Override
    public Object getValue() {
      return uri;
    }

    @Override
    public Object getValueView() {
      return uri;
    }

    @Override
    public DataSource get() {
      return null;
    }

  }

}
