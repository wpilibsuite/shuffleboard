package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.CurvedArrow;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.Vector2D;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.plugin.base.data.MecanumDriveData;
import edu.wpi.first.shuffleboard.plugin.base.data.SpeedControllerData;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * Widget for viewing and controlling a mecanum drive base.
 */
@Description(name = "Mecanum Drivebase", dataTypes = MecanumDriveData.class)
@ParametrizedController("MecanumDriveWidget.fxml")
public final class MecanumDriveWidget extends AbstractDriveWidget<MecanumDriveData> {

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
  private static final int vectorRadius = 60;

  private final SpeedControllerWidget fl = Components.viewFor(SpeedControllerWidget.class).get();
  private final SpeedControllerWidget fr = Components.viewFor(SpeedControllerWidget.class).get();
  private final SpeedControllerWidget rl = Components.viewFor(SpeedControllerWidget.class).get();
  private final SpeedControllerWidget rr = Components.viewFor(SpeedControllerWidget.class).get();
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
        .map(s -> motorSource(s,
            "Front Left Motor",
            MecanumDriveData::getFrontLeftSpeed,
            MecanumDriveData::withFrontLeftSpeed))
        .orElse(DataSource.none());
    frontRightMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> motorSource(s,
            "Front Right Motor",
            MecanumDriveData::getFrontRightSpeed,
            MecanumDriveData::withFrontRightSpeed))
        .orElse(DataSource.none());
    rearLeftMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> motorSource(s,
            "Rear Left Motor",
            MecanumDriveData::getRearLeftSpeed,
            MecanumDriveData::withRearLeftSpeed))
        .orElse(DataSource.none());
    rearRightMotorSource = EasyBind.monadic(typedSourceProperty())
        .map(s -> motorSource(s,
            "Rear Right Motor",
            MecanumDriveData::getRearRightSpeed,
            MecanumDriveData::withRearRightSpeed))
        .orElse(DataSource.none());

    fl.sourceProperty().bind(frontLeftMotorSource);
    fr.sourceProperty().bind(frontRightMotorSource);
    rl.sourceProperty().bind(rearLeftMotorSource);
    rr.sourceProperty().bind(rearRightMotorSource);

    overrideWidgetSize(fl, fr, rl, rr);

    leftControls.getChildren().addAll(fl.getView(), rl.getView());
    rightControls.getChildren().addAll(fr.getView(), rr.getView());

    dataOrDefault.addListener((__, prev, cur) -> {
      if (showForceVectors.get()) {
        drawForceVectors(cur);
      }
    });

    showForceVectors.addListener((__, was, is) -> {
      if (is) {
        MecanumDriveData data = dataOrDefault.get();
        drawForceVectors(data);
      } else {
        vectorPane.getChildren().clear();
      }
    });

    drawForceVectors(new MecanumDriveData(0, 0, 0, 0, false));

    driveView.getChildren().add(0, generateMecanumDriveBase(30, 80, 150, 200));
  }

  /**
   * Draws vectors displaying the forces generated by the mecanum wheels.
   */
  private void drawForceVectors(MecanumDriveData data) {
    vectorPane.getChildren().setAll(drawDirectionVector(data));
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Visuals",
            Setting.of("Show velocity vectors", showForceVectors, Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  private static Shape drawDirectionVector(MecanumDriveData data) {
    final Vector2D direction = data.getDirection();
    final double moment = data.getMoment();

    // Barely moving, draw an X
    if (Math.abs(moment) <= 0.01 && direction.getMagnitude() <= 0.01) {
      return generateX(25);
    }

    Shape rightMomentArrow = null;
    Shape leftMomentArrow = null;
    Shape directionArrow = null;

    if (Math.abs(moment) > 0.01) {
      // Only draw the moment vectors if the moment is significant enough
      rightMomentArrow = CurvedArrow.createPolar(0, vectorRadius, -moment * Math.PI, 0, 8);
      leftMomentArrow = CurvedArrow.createPolar(Math.PI, vectorRadius, -moment * Math.PI, 0, 8);
    }
    if (direction.getMagnitude() > 0.01) {
      // Only draw the direction vector if it'd be long enough
      directionArrow = CurvedArrow.createStraight(direction.getMagnitude() * vectorRadius, -direction.getAngle(), 0, 8);
    }

    Shape result = union(rightMomentArrow, leftMomentArrow, directionArrow);
    result.getStyleClass().add("robot-direction-vector");
    return result;
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

}
