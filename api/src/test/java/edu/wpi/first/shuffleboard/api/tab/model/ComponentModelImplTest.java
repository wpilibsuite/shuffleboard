package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ComponentModelImplTest {

  @Test
  public void testPath() {
    ComponentModel component = new ComponentModelImpl("path", null, null, Collections.emptyMap());
    assertEquals("path", component.getPath());
  }

  @Test
  public void testParent() {
    ParentModel parent = new TabModelImpl("Tab");
    ComponentModel component = new ComponentModelImpl("", parent, null, Collections.emptyMap());
    assertSame(parent, component.getParent());
  }

  @Test
  public void testDisplayType() {
    String displayType = "Display Type";
    String newDisplayType = "New Display Type";

    ComponentModel component = new ComponentModelImpl("", null, displayType, Collections.emptyMap());
    assertEquals(displayType, component.getDisplayType());

    component.setDisplayType(newDisplayType);
    assertEquals(newDisplayType, component.getDisplayType());
  }

  @Test
  public void testProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("foo", "bar");
    ComponentModel component = new ComponentModelImpl("", null, null, properties);
    assertEquals(properties, component.getProperties());

    Map<String, Object> newProperties = new HashMap<>();
    newProperties.put("bar", "baz");
    component.setProperties(newProperties);
    assertEquals(newProperties, component.getProperties());
  }

  @Test
  public void testPreferredPosition() {
    ComponentModel component = new ComponentModelImpl("", null, "", Collections.emptyMap());
    component.setPreferredPosition(new GridPoint(5, 5));
    assertEquals(new GridPoint(5, 5), component.getPreferredPosition());
  }

  @Test
  public void testPreferredSize() {
    ComponentModel component = new ComponentModelImpl("", null, "", Collections.emptyMap());
    component.setPreferredSize(new TileSize(1, 1));
    assertEquals(new TileSize(1, 1), component.getPreferredSize());
  }

  @Test
  public void testTitle() {
    ComponentModel component = new ComponentModelImpl("/Shuffleboard/Tab/X/Y/Foo", null, null, Collections.emptyMap());
    assertEquals("Foo", component.getTitle());
  }

}
