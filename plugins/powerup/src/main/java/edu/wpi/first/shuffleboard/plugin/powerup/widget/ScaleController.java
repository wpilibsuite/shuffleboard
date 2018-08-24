package edu.wpi.first.shuffleboard.plugin.powerup.widget;

import edu.wpi.first.shuffleboard.api.util.PseudoClassProperty;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.Alliance;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public final class ScaleController extends ElementController {

  @FXML
  private Pane root;
  @FXML
  private Pane nearPlatform;
  @FXML
  private Pane farPlatform;

  private final Property<Alliance> alliance = new SimpleObjectProperty<>(this, "alliance", null);
  private BooleanProperty nearRed;  // NOPMD - field avoids GC
  private BooleanProperty nearBlue; // NOPMD - field avoids GC
  private BooleanProperty farRed;   // NOPMD - field avoids GC
  private BooleanProperty farBlue;  // NOPMD - field avoids GC

  @FXML
  @Override
  protected void initialize() {
    super.initialize();
    nearRed = new PseudoClassProperty(nearPlatform, "red");
    nearBlue = new PseudoClassProperty(nearPlatform, "blue");
    farRed = new PseudoClassProperty(farPlatform, "red");
    farBlue = new PseudoClassProperty(farPlatform, "blue");

    nearRed.bind(EasyBind.monadic(alliance).map(Alliance.RED::equals));
    nearBlue.bind(EasyBind.monadic(alliance).map(Alliance.BLUE::equals));
    farRed.bind(nearBlue);
    farBlue.bind(nearRed);

    root.getProperties().put("fx:controller", this);
  }

  public Alliance getAlliance() {
    return alliance.getValue();
  }

  public Property<Alliance> allianceProperty() {
    return alliance;
  }

  public void setAlliance(Alliance alliance) {
    this.alliance.setValue(alliance);
  }
}
