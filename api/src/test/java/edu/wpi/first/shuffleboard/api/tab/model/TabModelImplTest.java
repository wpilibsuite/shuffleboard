package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TabModelImplTest {

  @Test
  public void testTitle() {
    String title = "Tab Title Foo";
    TabModel tab = new TabModelImpl(title);
    assertEquals(title, tab.getTitle());
  }

  @Test
  public void testProperties() {
    TabModel tab = new TabModelImpl("");

    assertTrue(tab.getProperties().isEmpty(), "Tab should have no initial properties");

    Map<String, Object> properties = new HashMap<>();
    properties.put("foo", "bar");
    tab.setProperties(properties);
    assertEquals(properties, tab.getProperties(), "Properties not set correctly");

    Map<String, Object> newProperties = new HashMap<>();
    newProperties.put("bar", "baz");
    tab.setProperties(newProperties);
    assertEquals(newProperties, tab.getProperties(), "Properties not updated correctly");
  }

  @Test
  public void testGetLayout() {
    TabModel tab = new TabModelImpl("Tab");
    String path = "/Shuffleboard/Tab/List";
    LayoutModel layout = tab.getLayout(path, "List Layout");
    assertAll(
        () -> assertSame(layout, tab.getChild(path)),
        () -> assertSame(layout, tab.getChildren().get(path))
    );
  }

  @Test
  public void testGetChildNotPresent() {
    TabModel tab = new TabModelImpl("Tab");
    String path = "/Shuffleboard/Tab/Foo";
    ComponentModel child = tab.getChild(path);
    assertNull(child);
  }

  @Test
  public void testAddChild() {
    TabModel tab = new TabModelImpl("Tab");
    String path = "/Shuffleboard/Tab/Foo";
    ComponentModel child = new WidgetModelImpl(path, tab, () -> null, "", Collections.emptyMap());
    tab.addChild(child);
    assertSame(child, tab.getChild(path));
  }

  @Test
  public void testGetOrCreate() {
    TabModel tab = new TabModelImpl("Tab");
    WidgetModel widget = tab.getOrCreate("/Shuffleboard/Tab/Foo", DataSource::none, "", Collections.emptyMap());
    assertSame(widget, tab.getChild("/Shuffleboard/Tab/Foo"));
  }

  @Test
  public void testGetOrCreateExistingLayout() {
    TabModel tab = new TabModelImpl("Tab");
    tab.getLayout("/", "");
    assertThrows(IllegalArgumentException.class, () -> tab.getOrCreate("/", null, null, null));
  }

  @Test
  public void testGetOrCreateUpdatesExistingWidget() {
    TabModel tab = new TabModelImpl("Tab");
    WidgetModel widget = tab.getOrCreate("/", DataSource::none, "", Collections.emptyMap());

    Map<String, Object> newProperties = new HashMap<>();
    newProperties.put("foo", "bar");
    WidgetModel second = tab.getOrCreate("/", DataSource::none, "List", newProperties);

    assertAll("Updates",
        () -> assertSame(widget, second, "Second getOrCreate returned a different object"),
        () -> assertEquals("List", widget.getDisplayType()),
        () -> assertEquals(newProperties, widget.getProperties())
    );
  }

}
