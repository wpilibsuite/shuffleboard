package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.data.SendableChooserData;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;

@Description(name = "ComboBox Chooser", dataTypes = DataType.SendableChooser)
@ParametrizedController("ComboBoxChooser.fxml")
public class ComboBoxChooser extends ComplexAnnotatedWidget<SendableChooserData> {

  @FXML
  private Pane root;
  @FXML
  ComboBox<String> comboBox;

  @FXML
  private void initialize() {
    dataProperty().addListener((observable, oldData, newData) -> {
      newData.optionsProperty().addListener((__, oldOptions, newOptions) -> {
        updateOptions(newOptions);
      });
      newData.defaultOptionProperty().addListener((__, oldDefault, newDefault) -> {
        updateDefaultValue(newDefault);
      });
      newData.selectedOptionProperty().addListener((__, oldSelected, newSelected) -> {
        updateSelectedValue(newSelected);
      });
      updateOptions(newData.getOptions());
      updateDefaultValue(newData.getDefaultOption());
      updateSelectedValue(newData.getSelectedOption());
    });
    comboBox.getSelectionModel()
        .selectedItemProperty()
        .addListener((__, oldValue, newValue) -> getData().setSelectedOption(newValue));
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
