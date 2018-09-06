package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LayoutModelImplTest {

  private ParentModel parent;

  @BeforeEach
  public void setup() {
    parent = new TabModelImpl("Tab");
  }

  @Test
  public void testParent() {
    LayoutModel layout = new LayoutModelImpl("Layout", parent, "List");
    assertSame(parent, layout.getParent());
  }

  @Test
  public void testGetLayout() {
    LayoutModel layout = new LayoutModelImpl("Layout", parent, "List");
    String path = "/Shuffleboard/Tab/List";
    LayoutModel nested = layout.getLayout(path, "List Layout");
    assertAll(
        () -> assertSame(nested, layout.getChild(path)),
        () -> assertSame(nested, layout.getChildren().get(path))
    );
  }

  @Test
  public void testGetChildNotPresent() {
    LayoutModel layout = new LayoutModelImpl("Layout", parent, "List");
    String path = "/Shuffleboard/Tab/Foo";
    ComponentModel child = layout.getChild(path);
    assertNull(child);
  }

  @Test
  public void testAddChild() {
    LayoutModel layout = new LayoutModelImpl("Layout", parent, "List");
    String path = "/Shuffleboard/Tab/Foo";
    ComponentModel child = new WidgetModelImpl(path, layout, () -> null, "", Collections.emptyMap());
    layout.addChild(child);
    assertSame(child, layout.getChild(path));
  }

  @Test
  public void testGetOrCreate() {
    LayoutModel layout = new LayoutModelImpl("Layout", parent, "List");
    WidgetModel widget = layout.getOrCreate("/Shuffleboard/Tab/Foo", DataSource::none, "", Collections.emptyMap());
    assertSame(widget, layout.getChild("/Shuffleboard/Tab/Foo"));
  }
}
