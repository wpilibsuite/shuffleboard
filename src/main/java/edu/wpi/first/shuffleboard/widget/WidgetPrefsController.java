package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.components.PreferencesPane;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class WidgetPrefsController {

  @FXML
  private PreferencesPane preferencesPane;

  @FXML
  private void initialize() {
    // trigger all the editors when the pane is hidden so we can avoid "save" and "cancel" buttons
    preferencesPane.managedProperty().addListener((__, wasManaged, isManaged) -> {
      if (wasManaged) {
        preferencesPane.getEditorControls()
                       .forEach(control -> control.fireEvent(new ActionEvent(this, control)));
      }
    });
  }

}
