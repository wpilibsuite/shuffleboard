package edu.wpi.first.shuffleboard.plugin.powerup.widget;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

public class SwitchController extends ElementController {

  @FXML
  private Pane root;

  @FXML
  public void initialize() {
    super.initialize();
    root.getProperties().put("fx:controller", this);
  }

}
