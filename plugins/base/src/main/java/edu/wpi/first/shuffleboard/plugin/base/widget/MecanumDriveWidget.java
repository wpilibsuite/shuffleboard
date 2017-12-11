package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SubSource;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.MecanumDriveData;
import edu.wpi.first.shuffleboard.plugin.base.data.SpeedControllerData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SpeedControllerType;

import com.google.common.annotations.VisibleForTesting;
import com.sun.javafx.geom.Vec2d;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Widget for viewing and controlling a mecanum drive base.
 */
@Description(name = "Mecanum Drivebase", dataTypes = MecanumDriveData.class)
@ParametrizedController("MecanumDriveWidget.fxml")
public final class MecanumDriveWidget extends SimpleAnnotatedWidget<MecanumDriveData> {

  @FXML
  private Pane root;
  @FXML
  private VBox leftControls;
  @FXML
  private StackPane driveView;
  @FXML
  private VBox rightControls;
  @FXML
  private Pane vectorPane;

  private final BooleanProperty showForceVectors = new SimpleBooleanProperty(this, "showForceVectors", true);

  private final SpeedController fl = Components.viewFor(SpeedController.class).get();
  private final SpeedController fr = Components.viewFor(SpeedController.class).get();
  private final SpeedController rl = Components.viewFor(SpeedController.class).get();
  private final SpeedController rr = Components.viewFor(SpeedController.class).get();
  private MonadicBinding<DataSource<SpeedControllerData>> frontLeftMotorSource;  //NOPMD could be local variable
  private MonadicBinding<DataSource<SpeedControllerData>> frontRightMotorSource; //NOPMD could be local variable
  private MonadicBinding<DataSource<SpeedControllerData>> rearLeftMotorSource;   //NOPMD could be local variable
  private MonadicBinding<DataSource<SpeedControllerData>> rearRightMotorSource;  //NOPMD could be local variable

  /**
   * Marks the positions of mecanum wheels on a drive base. This is used to determine the orientation to draw a wheel.
   * Note that {@link #FRONT_LEFT} and {@link #REAR_RIGHT} are functionally identical, as well as
   * {@link #FRONT_RIGHT} and {@link #REAR_LEFT}.
   */
  private enum MecanumWheelPos {
    FRONT_LEFT,
    FRONT_RIGHT,
    REAR_LEFT,
    REAR_RIGHT
  }

  @FXML
  private void initialize() {
    frontLeftMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> sourceFor(s,
            "Front Left Motor",
            MecanumDriveData::getFrontLeftSpeed,
            MecanumDriveData::withFrontLeftSpeed))
        .orElse(DataSource.none());
    frontRightMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> sourceFor(s,
            "Front Right Motor",
            MecanumDriveData::getFrontRightSpeed,
            MecanumDriveData::withFrontRightSpeed))
        .orElse(DataSource.none());
    rearLeftMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> sourceFor(s,
            "Rear Left Motor",
            MecanumDriveData::getRearLeftSpeed,
            MecanumDriveData::withRearLeftSpeed))
        .orElse(DataSource.none());
    rearRightMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> sourceFor(s,
            "Rear Right Motor",
            MecanumDriveData::getRearRightSpeed,
            MecanumDriveData::withRearRightSpeed))
        .orElse(DataSource.none());

    fl.sourceProperty().bind(frontLeftMotorSource);
    fr.sourceProperty().bind(frontRightMotorSource);
    rl.sourceProperty().bind(rearLeftMotorSource);
    rr.sourceProperty().bind(rearRightMotorSource);

    leftControls.getChildren().addAll(fl.getView(), rl.getView());
    rightControls.getChildren().addAll(fr.getView(), rr.getView());

    dataOrDefault.addListener((__, prev, cur) -> {
      Vec2d direction = cur.getDirection();
      double moment = cur.getMoment();
      System.out.println("Data:      " + cur);
      System.out.println("Direction: " + direction);
      System.out.println("Moment:    " + moment);
      System.out.println("Turn:      " + cur.getTurn());
      if (showForceVectors.get()) {
        drawForceVectors(direction, moment);
      }
    });

    showForceVectors.addListener((__, was, is) -> {
      if (is) {
        MecanumDriveData data = dataOrDefault.get();
        drawForceVectors(data.getDirection(), data.getMoment());
      } else {
        vectorPane.getChildren().clear();
      }
    });

    driveView.getChildren().add(0, generateMecanumDriveBase(30, 80, 150, 200));
    exportProperties(showForceVectors);
  }

  /**
   * Creates a subsource for a specific motor in a mecanum drive.
   *
   * @param mecanumSource the source of the mecanum drive data
   * @param motor         the name of the motor
   * @param getter        the getter (eg {@code MecanumDriveData::getFrontLeftSpeed})
   * @param setter        the setter (eg {@code MecanumDriveData::withFrontLeftSpeed})
   */
  private DataSource<SpeedControllerData> sourceFor(DataSource<MecanumDriveData> mecanumSource,
                                                    String motor,
                                                    ToDoubleFunction<MecanumDriveData> getter,
                                                    BiFunction<MecanumDriveData, Double, MecanumDriveData> setter) {
    Function<MecanumDriveData, SpeedControllerData> from =
        d -> new SpeedControllerData(motor, d == null ? 0 : getter.applyAsDouble(d));
    Function<SpeedControllerData, MecanumDriveData> to =
        d -> setter.apply(mecanumSource.getData(), d == null ? 0.0 : d.getValue());
    return new SubSource<>(new SpeedControllerType(), mecanumSource, to, from);
  }

  /**
   * Draws the direction and moment vectors.
   *
   * @param direction the direction of the drive base
   * @param moment    the turning moment of the drive base
   */
  private void drawForceVectors(Vec2d direction, double moment) {
    vectorPane.getChildren().clear();
    if (moment != 0) {
      vectorPane.getChildren().add(drawMomentArcs(moment));
    }
    if (direction.x != 0 || direction.y != 0) {
      vectorPane.getChildren().add(drawDirectionVector(direction));
    }
  }

  @Override
  public Pane getView() {
    return root;
  }

  private static Shape drawMomentArcs(double moment) {
    final int radius = 60;
    final double arcLength = moment * radius * Math.PI;
    Arc rightArc = new Arc();
    rightArc.setType(ArcType.OPEN);
    rightArc.setFill(null);
    rightArc.setStroke(Color.WHITE);
    rightArc.setStartAngle(0);
    rightArc.setRadiusX(radius);
    rightArc.setRadiusY(radius);
    rightArc.setLength(arcLength);

    Arc leftArc = new Arc();
    leftArc.setType(ArcType.OPEN);
    leftArc.setFill(null);
    leftArc.setStroke(Color.WHITE);
    leftArc.setStartAngle(180);
    leftArc.setRadiusX(radius);
    leftArc.setRadiusY(radius);
    leftArc.setLength(arcLength);

    Shape arcs = union(rightArc, leftArc);
    arcs.getStyleClass().add("robot-moment-arc");
    return arcs;
  }

  private static Shape drawDirectionVector(Vec2d directionVector) {
    Line shaft = new Line(0, 0, directionVector.x * 60, -directionVector.y * 60);
    shaft.getStyleClass().add("robot-direction-vector");
    return shaft;
  }

  /**
   * Generates a mecanum drive base.
   *
   * @param wheelWidth  the width (or thickness) of the wheels
   * @param wheelHeight the height (or diameter) of the wheels
   * @param frameWidth  the width of the robot frame
   * @param frameHeight the height of the robot frame
   */
  private static Shape generateMecanumDriveBase(double wheelWidth,
                                                double wheelHeight,
                                                double frameWidth,
                                                double frameHeight) {
    Rectangle frame = new Rectangle(wheelWidth, 0, frameWidth, frameHeight);
    frame.setFill(null);
    frame.setStroke(Color.WHITE);

    final Shape fl = mecanumWheel(wheelWidth, wheelHeight, MecanumWheelPos.FRONT_LEFT);
    final Shape fr = mecanumWheel(wheelWidth, wheelHeight, MecanumWheelPos.FRONT_RIGHT);
    final Shape rl = mecanumWheel(wheelWidth, wheelHeight, MecanumWheelPos.REAR_LEFT);
    final Shape rr = mecanumWheel(wheelWidth, wheelHeight, MecanumWheelPos.REAR_RIGHT);
    fr.setTranslateX(frameWidth + wheelWidth);
    rl.setTranslateY(frameHeight - wheelHeight);
    rr.setTranslateX(frameWidth + wheelWidth);
    rr.setTranslateY(frameHeight - wheelHeight);

    Shape combined = union(frame, fl, fr, rl, rr);
    combined.getStyleClass().addAll("robot-drive", "mecanum-drive");
    return combined;
  }

  /**
   * Generates a stylized image of a mecanum wheel.
   *
   * @param width  the width of the wheel
   * @param height the height of the wheel
   * @param pos    the position of the wheel
   */
  private static Shape mecanumWheel(double width, double height, MecanumWheelPos pos) {
    int numLines = 5;
    double lineSpacing = height / (numLines + 1);
    Rectangle rectangle = new Rectangle(width, height);
    Rectangle boundingBox = new Rectangle(width, height);
    boundingBox.setFill(Color.WHITE);
    boundingBox.setStroke(Color.WHITE);
    rectangle.setStroke(Color.WHITE);
    rectangle.setFill(null);
    Shape result = rectangle;
    for (int i = 0; i <= numLines + 1; i++) {
      Line line;
      switch (pos) {
        case FRONT_LEFT:
        case REAR_RIGHT:
          line = new Line(0, lineSpacing * (i - 1), width, lineSpacing * (i + 1));
          break;
        case FRONT_RIGHT:
        case REAR_LEFT:
          line = new Line(0, lineSpacing * (i + 1), width, lineSpacing * (i - 1));
          break;
        default:
          throw new IllegalArgumentException("Unknown mecanum wheel position: " + pos);
      }
      line.setStroke(Color.WHITE);
      line.setFill(null);
      Shape intersect = Shape.intersect(boundingBox, line); // Make sure the line doesn't go outside the wheel
      result = union(result, intersect);
    }
    result.getStyleClass().addAll("wheel", "mecanum-wheel");
    return result;
  }

  /**
   * Performs a union of an arbitrary amount of shapes.
   *
   * @param shapes the shapes to union
   *
   * @return the shape resulting from doing a union of the given shapes
   */
  @VisibleForTesting
  static Shape union(Shape... shapes) {
    switch (shapes.length) {
      case 0:
        throw new IllegalArgumentException("No shapes to union");
      case 1:
        return shapes[0];
      case 2:
        return Shape.union(shapes[0], shapes[1]);
      default:
        Shape union = shapes[0];
        for (int i = 1; i < shapes.length; i++) {
          Shape shape = shapes[i];
          union = Shape.union(union, shape);
        }
        return union;
    }
  }

}
