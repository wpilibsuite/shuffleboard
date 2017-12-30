package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

@Description(
    name = "Boolean Box",
    dataTypes = Boolean.class)
@ParametrizedController("BooleanBoxWidget.fxml")
public class BooleanBoxWidget extends SimpleAnnotatedWidget<Boolean> {

  @FXML
  private Pane root;

  private final Property<Color> trueColor
      = new SimpleObjectProperty<>(this, "colorWhenTrue", Color.LAWNGREEN);
  private final Property<Color> falseColor
      = new SimpleObjectProperty<>(this, "colorWhenFalse", Color.DARKRED);

  @FXML
  private void initialize() {
    root.backgroundProperty().bind(
        Bindings.createObjectBinding(
            () -> createSolidColorBackground(getColor()),
            dataProperty(), trueColor, falseColor));
    exportProperties(trueColor, falseColor);
  }

  @Override
  public Pane getView() {
    return root;
  }

  private Color getColor() {
    final Boolean data = getData();
    if (data == null) {
      return Color.BLACK;
    }

    if (data) {
      return trueColor.getValue();
    } else {
      return falseColor.getValue();
    }
  }

  private Background createSolidColorBackground(Color color) {
    return new Background(new BackgroundFill(color, null, null));
  }

  public Color getTrueColor() {
    return trueColor.getValue();
  }

  public Property<Color> trueColorProperty() {
    return trueColor;
  }

  public void setTrueColor(Color trueColor) {
    this.trueColor.setValue(trueColor);
  }

  public Color getFalseColor() {
    return falseColor.getValue();
  }

  public Property<Color> falseColorProperty() {
    return falseColor;
  }

  public void setFalseColor(Color falseColor) {
    this.falseColor.setValue(falseColor);
  }

}
