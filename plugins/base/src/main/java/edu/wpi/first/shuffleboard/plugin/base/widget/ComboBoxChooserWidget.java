package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.ComplexAnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.plugin.base.data.SendableChooserData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SendableChooserType;

import com.google.common.annotations.VisibleForTesting;

import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import java.util.Map;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

@Description(name = "ComboBox Chooser", dataTypes = SendableChooserType.class)
@ParametrizedController("ComboBoxChooserWidget.fxml")
public class ComboBoxChooserWidget extends ComplexAnnotatedWidget<SendableChooserData> {

  private static final GlyphFont fontAwesome = GlyphFontRegistry.font("FontAwesome");
  private static final PseudoClass error = PseudoClass.getPseudoClass("error");

  @FXML
  private Pane root;
  @FXML
  @VisibleForTesting
  ComboBox<String> comboBox;
  @FXML
  private Pane selectionLabelContainer;

  private final Tooltip activeTooltip = new Tooltip();

  @FXML
  private void initialize() {
    dataOrDefault.addListener((__, oldData, newData) -> {
      final Map<String, Object> changes = newData.changesFrom(oldData);
      if (changes.containsKey(SendableChooserData.OPTIONS_KEY)) {
        updateOptions(newData.getOptions());
      }
      if (changes.containsKey(SendableChooserData.DEFAULT_OPTION_KEY)) {
        updateDefaultValue(newData.getDefaultOption());
      }
      if (changes.containsKey(SendableChooserData.SELECTED_OPTION_KEY)) {
        updateSelectedValue(newData.getSelectedOption());
      }
      confirmationLabel(newData.getActiveOption().equals(newData.getSelectedOption()));
    });
    activeTooltip.textProperty().bind(
        dataOrDefault
            .map(SendableChooserData::getActiveOption)
            .map(option -> "Active option: '" + option + "'"));
    comboBox.getSelectionModel()
        .selectedItemProperty()
        .addListener((__, oldValue, newValue) -> {
          SendableChooserData currentData = getData();
          if (newValue == null) {
            String defaultOption = currentData.getDefaultOption();
            setData(currentData.withSelectedOption(defaultOption));
            comboBox.getSelectionModel().select(defaultOption);
          } else {
            setData(currentData.withSelectedOption(newValue));
          }
        });
  }

  private void confirmationLabel(boolean confirmation) {
    Label activeSelectionLabel;
    if (confirmation) {
      activeSelectionLabel = fontAwesome.create(FontAwesome.Glyph.CHECK);
    } else {
      activeSelectionLabel = fontAwesome.create(FontAwesome.Glyph.EXCLAMATION);
    }
    activeSelectionLabel.getStyleClass().add("confirmation-label");
    activeSelectionLabel.pseudoClassStateChanged(error, !confirmation);
    activeSelectionLabel.setTooltip(activeTooltip);
    selectionLabelContainer.getChildren().setAll(activeSelectionLabel);
  }

  private void updateOptions(String... options) {
    comboBox.getItems().setAll(options);
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
