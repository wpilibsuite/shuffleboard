package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SubSource;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.plugin.base.data.DriveBaseData;
import edu.wpi.first.shuffleboard.plugin.base.data.SpeedControllerData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SpeedControllerType;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

/**
 * An abstract class defining several shared methods for drive base widgets.
 */
public abstract class AbstractDriveWidget<T extends DriveBaseData<T>> extends SimpleAnnotatedWidget<T> {

  private static final Double ZERO = Double.valueOf(0.0);

  /**
   * Overrides the intrinsic minimum dimensions of some widgets. This is useful for sane resizing of
   */
  protected static void overrideWidgetSize(Widget... widgets) {
    Stream.of(widgets)
        .map(Widget::getView)
        .forEach(v -> {
          v.setMinWidth(Region.USE_COMPUTED_SIZE);
          v.setMinHeight(Region.USE_COMPUTED_SIZE);
        });
  }

  /**
   * Creates a subsource for a specific motor in a drive base.
   *
   * @param source    the source for the drive base data
   * @param motorName the motor name (eg "Left Motor" or "Right Motor")
   * @param getter    the getter (eg {@code DifferentialDriveData::getLeftSpeed})
   * @param setter    the setter (eg {@code DifferentialDriveData::withLeftSpeed})
   */
  protected DataSource<SpeedControllerData> motorSource(DataSource<T> source,
                                                        String motorName,
                                                        ToDoubleFunction<T> getter,
                                                        BiFunction<T, Double, T> setter) {
    return new SubSource<>(
        SpeedControllerType.Instance,
        source,
        d -> setter.apply(dataOrDefault.get(), d == null ? ZERO : d.getValue()),
        t -> new SpeedControllerData(
            motorName,
            t == null ? ZERO : getter.applyAsDouble(t),
            t != null && t.isControllable()
        )
    );
  }

  /**
   * Generates an X-shape centered at <tt>(0, 0)</tt>.
   *
   * @param width the width of the X to generate
   */
  protected static Shape generateX(double width) {
    final double halfW = width / 2;
    Line lineA = new Line(-halfW, -halfW, halfW, halfW);
    Line lineB = new Line(-halfW, halfW, halfW, -halfW);
    Shape x = union(lineA, lineB);
    x.getStyleClass().add("robot-direction-vector");
    return x;
  }

  /**
   * Performs a union of an arbitrary amount of shapes.
   *
   * @param shapes the shapes to union
   *
   * @return the shape resulting from doing a union of the given shapes
   */
  protected static Shape union(Collection<? extends Shape> shapes) {
    return union(shapes.toArray(new Shape[shapes.size()]));
  }

  /**
   * Performs a union of an arbitrary amount of shapes.
   *
   * @param shapes the shapes to union
   *
   * @return the shape resulting from doing a union of the given shapes
   */
  protected static Shape union(Shape... shapes) {
    switch (shapes.length) {
      case 0:
        throw new IllegalArgumentException("No shapes to union");
      case 1:
        return shapes[0];
      case 2:
        if (shapes[0] == null) {
          return shapes[1];
        } else if (shapes[1] == null) {
          return shapes[0];
        } else {
          return Shape.union(shapes[0], shapes[1]);
        }
      default:
        Shape union = shapes[0];
        for (int i = 1; i < shapes.length; i++) {
          Shape shape = shapes[i];
          if (union == null) {
            union = shape;
          } else if (shape != null) {
            union = Shape.union(union, shape);
          }
        }
        return union;
    }
  }

}
