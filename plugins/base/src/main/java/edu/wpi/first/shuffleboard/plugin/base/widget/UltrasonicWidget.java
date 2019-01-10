package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.UltrasonicData;

import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

@Description(name = "Ultrasonic", dataTypes = UltrasonicData.class)
@ParametrizedController("UltrasonicWidget.fxml")
public final class UltrasonicWidget extends SimpleAnnotatedWidget<UltrasonicData> {

  @FXML
  private Pane root;
  @FXML
  private TextField data;

  private final Property<Unit> unit = new SimpleObjectProperty<>(this, "unit", Unit.INCH);

  private static final String FORMAT = "%.3f %s";

  @FXML
  private void initialize() {
    data.textProperty().bind(
        dataOrDefault.map(UltrasonicData::getRangeInches)
            .map(d -> Unit.INCH.as(d, getUnit()))
            .map(v -> String.format(FORMAT, v, getUnit().abbreviation)));
  }

  public Unit getUnit() {
    return unit.getValue();
  }

  @Override
  public List<Group> getSettings() {
    return List.of(
        Group.of(
            "Unit",
            Setting.of("Unit", "The unit to display the measured distance as", unit, Unit.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  public enum Unit {
    INCH(39.3701, "in"),
    FOOT(3.28084, "ft"),
    METER(1, "m"),
    MM(1000, "mm"),
    CM(100, "cm");

    private final double baseUnitEquivalent;
    public final String abbreviation;

    Unit(double baseUnitEquivalent, String abbreviation) {
      this.baseUnitEquivalent = baseUnitEquivalent;
      this.abbreviation = abbreviation;
    }

    public double as(double value, Unit unit) {
      return value * (unit.baseUnitEquivalent / this.baseUnitEquivalent);
    }
  }

}
