package edu.wpi.first.shuffleboard.plugin.powerup.widget;

import edu.wpi.first.shuffleboard.api.util.PseudoClassProperty;
import edu.wpi.first.shuffleboard.plugin.powerup.Configuration;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * Superclass for field element view controllers.
 */
public class ElementController {

  @FXML
  private Pane leftPlate;
  @FXML
  private Pane rightPlate;

  private final Property<Configuration> configuration =
      new SimpleObjectProperty<>(this, "configuration", Configuration.UNKNOWN);
  private BooleanProperty leftBlue;  // NOPMD - field avoids GC
  private BooleanProperty leftRed;   // NOPMD - field avoids GC
  private BooleanProperty rightRed;  // NOPMD - field avoids GC
  private BooleanProperty rightBlue; // NOPMD - field avoids GC

  @FXML
  protected void initialize() {
    leftPlate.getStyleClass().add("plate");
    rightPlate.getStyleClass().add("plate");
    leftPlate.pseudoClassStateChanged(PseudoClass.getPseudoClass("left"), true);
    rightPlate.pseudoClassStateChanged(PseudoClass.getPseudoClass("right"), true);

    leftRed = new PseudoClassProperty(leftPlate, "red");
    leftBlue = new PseudoClassProperty(leftPlate, "blue");
    rightRed = new PseudoClassProperty(rightPlate, "red");
    rightBlue = new PseudoClassProperty(rightPlate, "blue");

    leftRed.bind(EasyBind.monadic(configuration).map(c -> c == Configuration.RED_LEFT));
    rightRed.bind(EasyBind.monadic(configuration).map(c -> c == Configuration.RED_RIGHT));
    leftBlue.bind(rightRed);
    rightBlue.bind(leftRed);
  }

  public final Configuration getConfiguration() {
    return configuration.getValue();
  }

  public final Property<Configuration> configurationProperty() {
    return configuration;
  }

  public final void setConfiguration(Configuration configuration) {
    this.configuration.setValue(configuration);
  }

}
