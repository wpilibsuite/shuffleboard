package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabStructure;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.AbstractWidget;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

import com.google.common.collect.Iterables;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import javafx.scene.layout.Pane;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TabGeneratorTest {

  private NetworkTableInstance ntInstance;
  private NetworkTable rootTable;
  private NetworkTable rootMetaTable;
  private TabGenerator generator;
  private Components components;

  @BeforeEach
  public void setup() {
    ntInstance = NetworkTableInstance.create();
    ntInstance.setUpdateRate(0.01);
    rootTable = ntInstance.getTable("/Shuffleboard");
    rootMetaTable = rootTable.getSubTable(".metadata");
    components = new Components();
    generator = new TabGenerator(ntInstance, components);
    Components.setDefault(components);
    NetworkTableSourceType.setInstance(new NetworkTableSourceType(new NetworkTablesPlugin()));
    DataTypes.getDefault().getItems().forEach(t -> components.setDefaultComponent(t, new MockWidgetType()));
  }

  @AfterAll
  public static void tearDown() {
    Components.setDefault(new Components());
  }

  private void waitForNtUpdate() {
    if (!ntInstance.waitForEntryListenerQueue(0.5)) {
      fail("Timed out while waiting for entry listeners to fire");
    }
  }

  @Test
  public void testGenerateTabs() {
    generator.start();
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{"Foo", "Bar", "Baz"});
    waitForNtUpdate();
    Map<String, TabModel> tabs = generator.getStructure().getTabs();
    assertAll(
        () -> assertEquals(3, tabs.size(), "Wrong number of tabs generated"),
        () -> assertTrue(tabs.containsKey("Foo"), "No 'Foo' tab"),
        () -> assertTrue(tabs.containsKey("Bar"), "No 'Bar' tab"),
        () -> assertTrue(tabs.containsKey("Baz"), "No 'Baz' tab")
    );
  }

  @Test
  public void testSimpleWidgetInTab() {
    String tabName = "Tab";
    String widgetName = "Widget";
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{tabName});
    rootTable.getSubTable(tabName)
        .getEntry(".type")
        .setString(TabGenerator.TAB_TYPE);
    rootTable.getSubTable(tabName)
        .getEntry(widgetName)
        .setDouble(Math.PI);
    generator.start();
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    assertAll(
        () -> assertEquals(1, tabs.getTabs().size(), "Wrong number of tabs generated"),
        () -> assertNotNull(tabs.getTab(tabName).getChild(path(tabName, widgetName)))
    );
  }

  @Test
  public void testSimpleWidgetInLayout() {
    final String tabName = "Tab";
    final String layoutName = "Layout";
    final String layoutType = "LayoutType";
    final String widgetName = "Widget";

    components.register(new MockComponentType(layoutType));
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{tabName});
    rootTable.getSubTable(tabName)
        .getEntry(".type")
        .setString(TabGenerator.TAB_TYPE);
    rootTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getEntry(widgetName)
        .setDouble(12.34);
    rootTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getEntry(".type")
        .setString(TabGenerator.LAYOUT_TYPE);
    rootMetaTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getEntry(TabGenerator.PREF_COMPONENT_ENTRY_NAME)
        .setString(layoutType);
    generator.start();
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    ComponentModel layout = tabs.getTab(tabName).getChild(path(tabName, layoutName));
    ComponentModel widget = tabs.getTab(tabName).getChild(path(tabName, layoutName, widgetName));
    assertAll(
        () -> assertNotNull(layout, "Layout not generated"),
        () -> assertEquals(layoutType, layout.getDisplayType()),
        () -> assertNotNull(widget, "Widget not generated")
    );
  }

  @Test
  public void testComplexWidgetInLayout() {
    final String tabName = "Complex Layout";
    final String layoutName = "Layout";
    final String layoutType = "LayoutType";
    final String widgetName = "WidgetName";
    final String widgetType = "WidgetType";

    components.register(new MockComponentType(layoutType));
    components.register(new MockComponentType(widgetType));

    NetworkTable tabTable = rootTable.getSubTable(tabName);
    NetworkTable layoutTable = tabTable.getSubTable(layoutName);
    NetworkTable widgetTable = layoutTable.getSubTable(widgetName);

    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{tabName});
    tabTable.getEntry(".type").setString(TabGenerator.TAB_TYPE);
    layoutTable.getEntry(".type").setString(TabGenerator.LAYOUT_TYPE);
    rootMetaTable.getSubTable(tabName).getSubTable(layoutName)
        .getEntry(TabGenerator.PREF_COMPONENT_ENTRY_NAME)
        .setString(layoutType);
    widgetTable.getEntry(".type").setString(widgetType);
    widgetTable.getEntry("Value").setDouble(Math.E);
    NetworkTable widgetMetaTable = rootMetaTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getSubTable(widgetName);
    widgetMetaTable.getEntry(TabGenerator.PREF_COMPONENT_ENTRY_NAME)
        .setString(widgetType);

    generator.start();
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    ComponentModel widget = tabs.getTab(tabName).getChild(path(tabName, layoutName, widgetName));
    assertEquals(widgetType, widget.getDisplayType(), "Wrong display type");

    // ... and update
    widgetMetaTable.getEntry(TabGenerator.PREF_COMPONENT_ENTRY_NAME).setString("A Different Widget");
    waitForNtUpdate();
    assertEquals("A Different Widget", widget.getDisplayType(), "Widget type did not update");
  }

  @Test
  public void testWidgetWithSizeAndPosition() {
    final String tabName = "SizedWidget";
    final String widgetName = "Widget";
    final int col = 10;
    final int row = 2;
    final int width = 4;
    final int height = 1;

    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{tabName});
    NetworkTable widgetMetaTable = rootMetaTable.getSubTable(tabName).getSubTable(widgetName);
    widgetMetaTable.getEntry(TabGenerator.SIZE_ENTRY_NAME)
        .setDoubleArray(new double[]{width, height});
    widgetMetaTable.getEntry(TabGenerator.POSITION_ENTRY_NAME)
        .setDoubleArray(new double[]{col, row});
    rootTable.getSubTable(tabName)
        .getEntry(".type")
        .setString(TabGenerator.TAB_TYPE);
    rootTable.getSubTable(tabName)
        .getEntry(widgetName)
        .setString("foo");

    generator.start();
    waitForNtUpdate();

    ComponentModel widget = generator.getStructure().getTab(tabName).getChild(path(tabName, widgetName));
    assertAll("Size and position",
        () -> assertEquals(new TileSize(width, height), widget.getPreferredSize()),
        () -> assertEquals(new GridPoint(col, row), widget.getPreferredPosition())
    );

    // .. and update
    widgetMetaTable.getEntry(TabGenerator.SIZE_ENTRY_NAME).setDoubleArray(new double[]{width + 1, height + 1});
    widgetMetaTable.getEntry(TabGenerator.POSITION_ENTRY_NAME).setDoubleArray(new double[]{col + 1, row + 1});
    waitForNtUpdate();
    assertAll("Updated size and position",
        () -> assertEquals(new TileSize(width + 1, height + 1), widget.getPreferredSize()),
        () -> assertEquals(new GridPoint(col + 1, row + 1), widget.getPreferredPosition())
    );
  }

  @Test
  public void testTabProperties() {
    final String tabName = "TabWithProperties";

    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{tabName});
    NetworkTable propsTable = rootMetaTable.getSubTable(tabName).getSubTable(TabGenerator.PROPERTIES_TABLE_NAME);
    propsTable.getEntry("Foo").setDouble(12.34);
    propsTable.getEntry("Bar").setString("Baz");

    generator.start();
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    assertAll("Tab Properties",
        () -> assertFalse(tabs.getTabs().isEmpty(), "No tabs generated"),
        () -> assertEquals(12.34, tabs.getTab(tabName).getProperties().get("Foo")),
        () -> assertEquals("Baz", tabs.getTab(tabName).getProperties().get("Bar"))
    );
  }

  @Test
  public void testNestedWidgetWithProperties() {
    final String tabName = "Tab";
    final String layoutName = "Layout";
    final String widgetName = "Data";

    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{tabName});

    rootMetaTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getEntry(TabGenerator.PREF_COMPONENT_ENTRY_NAME)
        .setString("Layout");

    NetworkTable propsTable = rootMetaTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getSubTable(widgetName)
        .getSubTable(TabGenerator.PROPERTIES_TABLE_NAME);

    propsTable.getEntry("foo").setString("bar");

    rootTable.getSubTable(tabName)
        .getEntry(".type")
        .setString(TabGenerator.TAB_TYPE);
    rootTable.getSubTable(tabName)
        .getSubTable(layoutName).getEntry(".type")
        .setString(TabGenerator.LAYOUT_TYPE);
    rootTable.getSubTable(tabName)
        .getSubTable(layoutName)
        .getEntry(widgetName)
        .setDouble(123456);

    generator.start();
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    ComponentModel widget = tabs.getTab(tabName).getChild(path(tabName, layoutName, widgetName));
    assertEquals("bar", widget.getProperties().get("foo"));

    // update
    propsTable.getEntry("bar").setDouble(100);
    waitForNtUpdate();
    assertAll("Updated properties",
        () -> assertEquals("bar", widget.getProperties().get("foo")),
        () -> assertEquals(100.0, widget.getProperties().get("bar"))
    );
  }

  @Test
  public void testStop() {
    generator.start();
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{"Foo"});
    waitForNtUpdate();
    TabStructure tabs = generator.getStructure();
    assertAll(
        () -> assertEquals(1, tabs.getTabs().size()),
        () -> assertTrue(tabs.getTabs().containsKey("Foo"))
    );

    generator.stop();
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{"Foo", "Bar"});
    waitForNtUpdate();
    assertAll(
        () -> assertEquals(1, tabs.getTabs().size()),
        () -> assertTrue(tabs.getTabs().containsKey("Foo"))
    );
  }

  @Test
  public void testSelectTabByIndex() {
    int index = 1;
    generator.start();
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{"Foo", "Bar"});
    rootMetaTable.getEntry(TabGenerator.SELECTED_ENTRY_NAME).setDouble(index);
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    assertAll(
        () -> assertEquals(2, tabs.getTabs().size(), "Two tabs should have been created"),
        () -> assertEquals(index, tabs.getSelectedTabIndex(), "Tab was not selected"),
        () -> assertEquals(null, tabs.getSelectedTabTitle(), "Tab should not be selected by title")
    );
  }

  @Test
  public void testSelectTabByName() {
    String name = "Bar";
    generator.start();
    rootMetaTable.getEntry(TabGenerator.TABS_ENTRY_KEY).setStringArray(new String[]{"Foo", name});
    rootMetaTable.getEntry(TabGenerator.SELECTED_ENTRY_NAME).setString(name);
    waitForNtUpdate();

    TabStructure tabs = generator.getStructure();
    assertAll(
        () -> assertEquals(2, tabs.getTabs().size(), "Two tabs should have been created"),
        () -> assertEquals(-1, tabs.getSelectedTabIndex(),"Tab should not be selected by index"),
        () -> assertEquals(name, tabs.getSelectedTabTitle(), "Tab was not selected")
    );
  }

  private static String path(String tabName, String... children) {
    StringBuilder sb = new StringBuilder(TabGenerator.ROOT_TABLE_NAME)
        .append('/')
        .append(tabName);
    for (String child : children) {
      sb.append('/').append(child);
    }
    return sb.toString();
  }

  private static class MockComponentType implements ComponentType {
    private final String typeName;

    public MockComponentType(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public Class getType() {
      return null;
    }

    @Override
    public String getName() {
      return typeName;
    }

    @Override
    public Object get() {
      return null;
    }
  }

  private static final class MockWidget extends AbstractWidget {

    @Override
    public Pane getView() {
      return null;
    }

    @Override
    public String getName() {
      return "Mock Widget";
    }
  }

  private static final class MockWidgetType implements WidgetType<MockWidget> {

    @Override
    public Class<MockWidget> getType() {
      return MockWidget.class;
    }

    @Override
    public String getName() {
      return "Mock Widget";
    }

    @Override
    public Set<DataType> getDataTypes() {
      return Set.of();
    }

    @Override
    public MockWidget get() {
      return new MockWidget();
    }
  }
}
