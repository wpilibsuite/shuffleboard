package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.CurvedArrow;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.plugin.base.data.DifferentialDriveData;
import edu.wpi.first.shuffleboard.plugin.base.data.SpeedControllerData;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

@Description(name = "Differential Drivebase", dataTypes = DifferentialDriveData.class)
@ParametrizedController("DifferentialDriveWidget.fxml")
public final class DifferentialDriveWidget extends AbstractDriveWidget<DifferentialDriveData> {

  @FXML
  private HBox root;
  @FXML
  private Pane driveView;
  @FXML
  private Pane vectorPane;

  private final SpeedControllerWidget leftController = Components.viewFor(SpeedControllerWidget.class).get();
  private final SpeedControllerWidget rightController = Components.viewFor(SpeedControllerWidget.class).get();
  private MonadicBinding<DataSource<SpeedControllerData>> leftMotorSource;  //NOPMD could be local variable
  private MonadicBinding<DataSource<SpeedControllerData>> rightMotorSource; //NOPMD could be local variable

  private final BooleanProperty showVectors = new SimpleBooleanProperty(this, "showVectors", true);
  private final IntegerProperty numWheels = new SimpleIntegerProperty(this, "numberOfWheels", 4);
  private final DoubleProperty wheelHeight = new SimpleDoubleProperty(this, "wheelDiameter", 80);
  private static final double WHEEL_WIDTH = 30;
  private static final double FRAME_WIDTH = 150;
  private static final double FRAME_HEIGHT = 200;

  @FXML
  private void initialize() {
    leftMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s ->
            motorSource(s, "Left Motor", DifferentialDriveData::getLeftSpeed, DifferentialDriveData::withLeftSpeed))
        .orElse(DataSource.none());
    rightMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s ->
            motorSource(s, "Right Motor", DifferentialDriveData::getRightSpeed, DifferentialDriveData::withRightSpeed))
        .orElse(DataSource.none());

    overrideWidgetSize(leftController, rightController);

    leftController.setOrientation(Orientation.VERTICAL);
    rightController.setOrientation(Orientation.VERTICAL);

    leftController.sourceProperty().bind(leftMotorSource);
    rightController.sourceProperty().bind(rightMotorSource);

    root.getChildren().add(0, leftController.getView());
    root.getChildren().add(rightController.getView());
    numWheels.addListener((__, prev, cur) -> {
      int num = cur.intValue();
      if (num < 4 || num % 2 != 0) {
        numWheels.setValue(prev);
      } else {
        int wheelsPerSide = num / 2;
        double wheelHeight = this.wheelHeight.get();

        // Lower the wheel size to avoid overlaps
        if (wheelHeight * wheelsPerSide > FRAME_HEIGHT * 0.875) {
          this.wheelHeight.set((int) ((FRAME_HEIGHT / wheelsPerSide) * 0.875));
        }
        Shape driveBase = generateDifferentialDriveBase(
            WHEEL_WIDTH, this.wheelHeight.get(), FRAME_WIDTH, FRAME_HEIGHT, num);
        driveView.getChildren().clear();
        driveView.getChildren().setAll(driveBase, vectorPane);
      }
    });
    wheelHeight.addListener((__, prev, cur) -> {
      if (cur.doubleValue() < 0) {
        wheelHeight.setValue(prev);
      } else {
        Shape driveBase = generateDifferentialDriveBase(
            WHEEL_WIDTH, cur.doubleValue(), FRAME_WIDTH, FRAME_HEIGHT, numWheels.get());
        driveView.getChildren().clear();
        driveView.getChildren().setAll(driveBase, vectorPane);
      }
    });
    vectorPane.visibleProperty().bind(showVectors);
    dataOrDefault.addListener((__, prev, cur) -> {
      vectorPane.getChildren().setAll(drawMotionVector(cur.getLeftSpeed(), cur.getRightSpeed()));
    });
    vectorPane.getChildren().setAll(drawMotionVector(0, 0));
    driveView.getChildren().add(0, generateDifferentialDriveBase(
        WHEEL_WIDTH, wheelHeight.get(), FRAME_WIDTH, FRAME_HEIGHT, numWheels.get()));
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Wheels",
            Setting.of("Number of wheels", numWheels, Integer.class),
            Setting.of("Wheel diameter", wheelHeight, Double.class)
        ),
        Group.of("Visuals",
            Setting.of("Show velocity vectors", showVectors, Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  /**
   * Generates a wireframe view of a differential drive base.
   *
   * @param wheelWidth  the width of the wheels, in pixels
   * @param wheelHeight the height of the wheels, in pixels
   * @param frameWidth  the width of the frame, in pixels
   * @param frameHeight the height of the frame, in pixels
   * @param nWheels     the number of wheels. This must be an even number and be at least four.
   */
  private Shape generateDifferentialDriveBase(double wheelWidth,
                                              double wheelHeight,
                                              double frameWidth,
                                              double frameHeight,
                                              int nWheels) {
    if (nWheels < 4 || nWheels % 2 != 0) {
      throw new IllegalArgumentException(
          "The number of wheels must be an even number of at least four (4). Was given: " + nWheels);
    }
    final int wheelsPerSide = nWheels / 2;
    Shape frame = new Rectangle(frameWidth, frameHeight);
    frame.setFill(null);
    frame.setStroke(Color.WHITE);
    frame.setTranslateX(wheelWidth);
    boolean showAxes = false;
    if (showAxes) {
      Line x = new Line(wheelWidth, frameHeight / 2, frameWidth + wheelWidth, frameHeight / 2);
      Line y = new Line(frameWidth / 2 + wheelWidth, 0, frameWidth / 2 + wheelWidth, frameHeight);
      x.getStrokeDashArray().addAll(7.5, 7.5);
      y.getStrokeDashArray().addAll(7.5, 7.5);
      frame = union(frame, x, y);
    }
    double wheelSpacing = (frameHeight - wheelHeight) / (wheelsPerSide - 1);
    List<Shape> wheels = new ArrayList<>();
    for (int i = 0; i < wheelsPerSide; i++) {
      Shape wheel = new Rectangle(wheelWidth, wheelHeight);
      wheel.setFill(null);
      wheel.setStroke(Color.WHITE);
      wheel.setTranslateY(wheelSpacing * i);
      wheels.add(wheel);
    }
    for (int i = 0; i < wheelsPerSide; i++) {
      Shape wheel = new Rectangle(wheelWidth, wheelHeight);
      wheel.setFill(null);
      wheel.setStroke(Color.WHITE);
      wheel.setTranslateY(wheelSpacing * i);
      wheel.setTranslateX(frameWidth + wheelWidth);
      wheels.add(wheel);
    }
    Shape driveBase = union(frame, union(wheels));
    driveBase.getStyleClass().add("robot-drive");
    return driveBase;
  }

  @SuppressFBWarnings(value = "DLS_DEAD_LOCAL_STORE", justification = "FindBugs is bugged on final local variables")
  private Shape drawMotionVector(double left, double right) {
    // Barely moving, or not moving at all. Curved arrows look weird at low radii, so show an X instead
    if (Math.abs(left) <= 0.05 && Math.abs(right) <= 0.05) {
      return generateX(25);
    }

    // Max radius is half of the narrowest dimension, minus padding to avoid clipping with the frame
    final double maxRadius = Math.min(FRAME_WIDTH, FRAME_HEIGHT) / 2 - 8;
    final double arrowheadSize = 8;
    if (Math.abs(left - right) <= 0.001) {
      // Moving more-or-less straight (or not moving at all)
      // Using a threshold instead of a simpler `if(left == right)` avoids edge cases where left and right are very
      // close, which can cause floating-point issues with extremely large radii (on the order of 1E15 pixels)
      // and extremely small arc lengths (on the order of 1E-15 degrees)
      Shape arrow =
          CurvedArrow.createStraight(Math.abs(left * maxRadius), -Math.signum(left) * Math.PI / 2, 0, arrowheadSize);
      arrow.getStyleClass().add("robot-direction-vector");
      return arrow;
    }
    // Moving in an arc

    final double pi = Math.PI;
    final double moment = (right - left) / 2;
    final double avgSpeed = (left + right) / 2;
    final double turnRadius = avgSpeed / moment;

    final Shape arrow;

    if (Math.abs(turnRadius) >= 1) {
      // Motion is mostly forward/backward, and curving to a side

      final double arcSign = -Math.signum(turnRadius);  // +1 if arc is to left of frame, -1 if arc is to the right
      final double startAngle = (arcSign + 1) * pi / 2; // pi if arc is to the right, 0 if to the left
      double radius = Math.abs(turnRadius * maxRadius);
      arrow = CurvedArrow.create(startAngle, radius, arcSign * avgSpeed * maxRadius, arcSign * radius, arrowheadSize);
    } else {
      // Turning about a point inside the frame of the robot

      final double turnSign = Math.signum(left - right); // positive for clockwise, negative for counter-clockwise
      if (turnRadius == 0) {
        // Special case, rotating about the center of the frame
        double radius = Math.max(left, right) * maxRadius; // left == -right, we just want the positive one
        double angle = turnSign * pi;
        double start = moment < 0 ? pi : 0;
        arrow = CurvedArrow.createPolar(start, radius, angle, 0, arrowheadSize);
      } else {
        double dominant = turnRadius < 0 ? left : right;  // the dominant side that's driving the robot
        double secondary = turnRadius < 0 ? right : left; // the non-dominant side
        double radius = Math.abs(dominant) * maxRadius;   // make radius dependent on how fast the dominant side is
        double centerX = -turnRadius * radius;
        double angle = map(secondary / dominant, 0, -1, 0.5, pi);
        double start = turnRadius < 0 ? pi : 0;
        arrow = CurvedArrow.createPolar(start, radius, turnSign * angle, centerX, arrowheadSize);
      }
    }

    arrow.getStyleClass().add("robot-direction-vector");
    return arrow;
  }

  /**
   * Maps a value <i>linearly</i> from the range <tt>(minInput, maxInput)</tt> to <tt>(minOutput, maxOutput)</tt>.
   *
   * @param x         the value to map
   * @param minInput  the minimum value of the input range
   * @param maxInput  the maximum value of the input range
   * @param minOutput the minimum value of the output range
   * @param maxOutput the maximum value of the output range
   */
  @VisibleForTesting
  static double map(double x, double minInput, double maxInput, double minOutput, double maxOutput) {
    return (x - minInput) * (maxOutput - minOutput) / (maxInput - minInput) + minOutput;
  }

}
