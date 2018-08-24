package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.LayoutType;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.base.BasePlugin;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests all widgets with all possible data types to make sure they all
 */
@Tag("UI")
public class AllWidgetSanityTest extends ApplicationTest {

  private static final BasePlugin plugin = new BasePlugin();

  @BeforeAll
  public static void setup() {
    AsyncUtils.setAsyncRunner(Runnable::run);
    DataTypes.setDefault(new DataTypes());
    plugin.getDataTypes().forEach(DataTypes.getDefault()::register);
  }

  @AfterAll
  public static void tearDown() {
    DataTypes.setDefault(new DataTypes());
    AsyncUtils.setAsyncRunner(FxUtils::runOnFxThread);
  }

  @ParameterizedTest
  @MethodSource("createWidgetMap")
  public void testCreateWidget(WidgetType<?> widgetType, DataType<?> dataType) {
    Widget widget = widgetType.get();
    widget.addSource(DummySource.forType(dataType));
  }

  @ParameterizedTest
  @MethodSource("createWidgetMap")
  public void testNonNullView(WidgetType<?> widgetType, DataType<?> dataType) {
    Widget widget = widgetType.get();
    assertNotNull(widget.getView(), "No view for " + widgetType.getName());
  }

  @ParameterizedTest
  @MethodSource("createLayoutMap")
  public void testNonNullLayoutView(LayoutType<?> layoutType) {
    Layout layout = layoutType.get();
    assertNotNull(layout.getView(), "No view for " + layoutType.getName());
  }

  private static Stream<Arguments> createWidgetMap() {
    return plugin.getComponents().stream()
        .flatMap(TypeUtils.castStream(WidgetType.class))
        .flatMap(widgetType -> widgetType.getDataTypes().stream()
            .map(type -> Arguments.of(widgetType, type)));
  }

  private static Stream<Arguments> createLayoutMap() {
    return plugin.getComponents().stream()
        .flatMap(TypeUtils.castStream(LayoutType.class))
        .map(Arguments::of);
  }

}
