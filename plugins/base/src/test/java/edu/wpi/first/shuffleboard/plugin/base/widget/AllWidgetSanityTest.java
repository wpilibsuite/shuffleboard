package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.base.BasePlugin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.stream.Stream;

/**
 * Tests all widgets with all possible data types to make sure they all
 */
public class AllWidgetSanityTest extends ApplicationTest {

  private static final BasePlugin plugin = new BasePlugin();

  @BeforeAll
  public static void setup() {
    DataTypes.setDefault(new DataTypes());
    plugin.getDataTypes().forEach(DataTypes.getDefault()::register);
  }

  @AfterAll
  public static void tearDown() {
    DataTypes.setDefault(new DataTypes());
  }

  @ParameterizedTest
  @MethodSource("createWidgetMap")
  public void testCreateWidget(WidgetType widgetType, DataType<?> dataType) {
    Widget widget = widgetType.get();
    widget.setSource(DummySource.forType(dataType));
  }

  private static Stream<Arguments> createWidgetMap() {
    return plugin.getComponents().stream()
        .flatMap(TypeUtils.castStream(WidgetType.class))
        .flatMap(widgetType -> widgetType.getDataTypes().stream()
            .map(type -> Arguments.of(widgetType, type)));
  }

}
