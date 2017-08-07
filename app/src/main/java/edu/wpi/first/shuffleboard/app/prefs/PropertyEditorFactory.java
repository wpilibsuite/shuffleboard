package edu.wpi.first.shuffleboard.app.prefs;

import edu.wpi.first.shuffleboard.app.theme.DefaultThemes;
import edu.wpi.first.shuffleboard.app.theme.Theme;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import static javafx.collections.FXCollections.observableArrayList;

/**
 * A property editor factory that supports more than the default data types of
 * {@link DefaultPropertyEditorFactory}.
 */
public class PropertyEditorFactory extends DefaultPropertyEditorFactory {

  @Override
  public PropertyEditor<?> call(PropertySheet.Item item) {
    final Class<?> type = item.getType();
    if (type == Theme.class) {
      return new ThemePropertyEditor(item);
    }
    return super.call(item);
  }

  private static class ThemePropertyEditor extends AbstractPropertyEditor<Theme, ComboBox<Theme>> {

    ThemePropertyEditor(PropertySheet.Item property) {
      super(property, new ComboBox<>());
      getEditor().setItems(observableArrayList(DefaultThemes.getThemes()));
      getEditor().setConverter(new ThemeStringConverter());
    }

    @Override
    protected ObservableValue<Theme> getObservableValue() {
      return getEditor().getSelectionModel().selectedItemProperty();
    }

    @Override
    public void setValue(Theme value) {
      getEditor().getSelectionModel().select(value);
    }
  }

  private static class ThemeStringConverter extends StringConverter<Theme> {

    @Override
    public String toString(Theme object) {
      return object.getName();
    }

    @Override
    public Theme fromString(String string) {
      return DefaultThemes.forName(string, DefaultThemes.LIGHT);
    }
  }

}
