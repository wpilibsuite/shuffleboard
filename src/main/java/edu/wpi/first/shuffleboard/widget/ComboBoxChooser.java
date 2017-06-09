package edu.wpi.first.shuffleboard.widget;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

@Description(name = "ComboBox Chooser", dataTypes = DataType.SendableChooser)
@ParametrizedController("ComboBoxChooser.fxml")
public class ComboBoxChooser extends ComplexAnnotatedWidget {

  static final String DEFAULT_VALUE_KEY = "default";
  static final String SELECTED_VALUE_KEY = "selected";
  static final String OPTIONS_KEY = "options";

  @FXML
  private Pane root;
  @FXML
  ComboBox<String> comboBox;

  @FXML
  private void initialize() {
    dataProperty().addListener((observable, oldValue, newValue) -> {
      newValue.addListener((MapChangeListener<String, Object>) change -> {
        switch ((String) change.getKey()) {
          case OPTIONS_KEY:
            updateOptions((String[]) change.getMap().get(OPTIONS_KEY));
            break;
          case DEFAULT_VALUE_KEY:
            updateDefaultValue((String) change.getMap().get(DEFAULT_VALUE_KEY));
            break;
          case SELECTED_VALUE_KEY:
            updateSelectedValue((String) change.getMap().get(SELECTED_VALUE_KEY));
            break;
          default:
            // Nothing we can use
            break;
        }
      });
      updateOptions((String[]) newValue.getOrDefault(OPTIONS_KEY, new String[0]));
      updateDefaultValue((String) newValue.get(DEFAULT_VALUE_KEY));
      updateSelectedValue((String) newValue.get(SELECTED_VALUE_KEY));
    });
    comboBox.getSelectionModel()
        .selectedItemProperty()
        .addListener((__, oldValue, newValue) -> getData().put(SELECTED_VALUE_KEY, newValue));
  }

  private void updateOptions(String... options) {
    comboBox.setItems(FXCollections.observableArrayList(options));
  }

  private void updateDefaultValue(String defaultValue) {
    if (comboBox.getSelectionModel().getSelectedItem() == null) {
      comboBox.getSelectionModel().select(defaultValue);
    }
  }

  private void updateSelectedValue(String selectedValue) {
    comboBox.getSelectionModel().select(selectedValue);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
