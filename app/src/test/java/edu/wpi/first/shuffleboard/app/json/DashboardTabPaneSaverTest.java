package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.app.DashboardData;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.components.Tile;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("UI")
public class DashboardTabPaneSaverTest extends ApplicationTest {

  @BeforeAll
  public static void setup() {
    Components components = new Components();
    Components.setDefault(components);
    components.register(new DummyBooleanBoxType());
  }

  @AfterAll
  public static void tearDown() {
    Components.setDefault(new Components());
  }

  @Test
  @Tag("NonJenkinsTest")
  // Broken on Jenkins: class com.sun.javafx.jmx.HighlightRegion declares multiple JSON fields named hash
  // Possibly due to not being aware of the WidgetPaneSaver class? Unsure how that can happen unless there's a bug
  // with the classpath introspection utilities in Guava. Travis, Appveyor, and local (Window 10 x64) testing all work
  // as expected so it's most likely something weird with Jenkins (as of 2017-11-20)
  public void testDeserialize() throws Exception {
    Reader reader = new InputStreamReader(getClass().getResourceAsStream("/test.json"), "UTF-8");
    DashboardData data = JsonBuilder.forSaveFile().fromJson(reader, DashboardData.class);
    DashboardTabPane dashboard = data.getTabPane();

    assertEquals(2 + 1, dashboard.getTabs().size(), "There should be 2 dashboard tabs and 1 adder tab");

    DashboardTab firstTab = (DashboardTab) dashboard.getTabs().get(0);
    DashboardTab secondTab = (DashboardTab) dashboard.getTabs().get(1);
    assertEquals("First Tab", firstTab.getTitle(), "Title of the first tab was wrong");
    assertEquals("Second Tab", secondTab.getTitle(), "Title of the second tab was wrong");
    assertFalse(firstTab.isAutoPopulate(), "The first tab should not be autopopulating");
    assertTrue(secondTab.isAutoPopulate(), "The second tab should be autopopulating");
    assertEquals("", firstTab.getSourcePrefix(), "The first tab should not have a source prefix");
    assertEquals("foo", secondTab.getSourcePrefix(), "The second tab should autopopulate from 'foo'");
    assertEquals(1, firstTab.getWidgetPane().getTiles().size(), "There should only be one tile");
    Tile tile = firstTab.getWidgetPane().getTiles().get(0);
    Component content = tile.getContent();
    assertTrue(content instanceof DummyBooleanBox);
  }

  private static class DummyBooleanBoxType implements WidgetType<DummyBooleanBox> {
    @Override
    public Set<DataType> getDataTypes() {
      return null;
    }

    @Override
    public Class<DummyBooleanBox> getType() {
      return DummyBooleanBox.class;
    }

    @Override
    public String getName() {
      return "Boolean Box";
    }

    @Override
    public DummyBooleanBox get() {
      return new DummyBooleanBox();
    }
  }

  private static class DummyBooleanBox implements Widget {

    @Override
    public List<Group> getSettings() {
      return new ArrayList<>();
    }

    @Override
    public Pane getView() {
      return new Pane();
    }

    @Override
    public Property<String> titleProperty() {
      return new SimpleStringProperty();
    }

    @Override
    public String getName() {
      return "Boolean Box";
    }

    @Override
    public void addSource(DataSource source) throws IncompatibleSourceException {
      // NOP
    }

    @Override
    public ObservableList<DataSource> getSources() {
      return FXCollections.observableArrayList();
    }
  }

}
