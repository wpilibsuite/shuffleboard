package edu.wpi.first.shuffleboard.widget;

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
    dataTypes = DataType.Boolean)
@ParametrizedController("BooleanBox.fxml")
public class BooleanBox extends SimpleAnnotatedWidget<Boolean> {

  @FXML
  private Pane root;

  private final Property<Color> trueColor
      = new SimpleObjectProperty<>(this, "True Color", Color.LAWNGREEN);
  private final Property<Color> falseColor
      = new SimpleObjectProperty<>(this, "False Color", Color.DARKRED);

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
    } else if (data == true) { //NOPMD
      return trueColor.getValue();
    } else {
      return falseColor.getValue();
    }
  }

  private Background createSolidColorBackground(Color color) {
    return new Background(new BackgroundFill(color, null, null));
  }

}
