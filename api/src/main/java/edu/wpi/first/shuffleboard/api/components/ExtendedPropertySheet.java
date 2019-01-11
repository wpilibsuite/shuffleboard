package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.FxUtils;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.time.Duration;
import java.util.Optional;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * A version of {@link PropertySheet} that has better support for editing numbers (using {@link NumberField} and
 * {@link IntegerField} for doubles and integers, respectively) and booleans (using {@link ToggleSwitch}), and themes.
 */
public class ExtendedPropertySheet extends PropertySheet {

  /**
   * A custom editor factory that uses custom editors for text, numbers, booleans, and {@link Theme Themes}.
   */
  public static final Callback<Item, PropertyEditor<?>> CUSTOM_EDITOR_FACTORY = new DefaultPropertyEditorFactory() {
    @Override
    public PropertyEditor<?> call(Item item) {
      if (item.getType() == String.class) {
        return new TextPropertyEditor(item);
      }
      if (item.getType() == Integer.class) {
        return new IntegerPropertyEditor(item);
      }
      if (Number.class.isAssignableFrom(item.getType())) {
        return new NumberPropertyEditor(item);
      }
      if (item.getType() == Boolean.class) {
        return new ToggleSwitchEditor(item);
      }
      if (item.getType() == Theme.class) {
        return new ThemePropertyEditor(item);
      }
      return super.call(item);
    }
  };

  /**
   * Creates an empty property sheet.
   */
  public ExtendedPropertySheet() {
    super();
    setModeSwitcherVisible(false);
    setSearchBoxVisible(false);
    setPropertyEditorFactory(CUSTOM_EDITOR_FACTORY);
  }

  /**
   * Creates a property sheet for editing the settings in a category. This does <i>not</i> include settings for
   * subcategories.
   */
  public ExtendedPropertySheet(Category settingsCategory) {
    this();
    setMode(PropertySheet.Mode.CATEGORY);
    for (Group group : settingsCategory.getGroups()) {
      for (Setting<?> setting : group.getSettings()) {
        PropertySheet.Item item = new SettingsItem(group, setting);
        getItems().add(item);
      }
    }
  }

  /**
   * An item backed by a JavaFX property.
   */
  public static class PropertyItem<T> implements Item {

    private final Property<T> property;
    private final String name;

    /**
     * Creates a new PropertyItem from the given property. The name of the item is generated from the name of the
     * property by converting from <tt>camelCase</tt> to a natural-language <tt>Sentence case</tt> text. For example,
     * a property with the name <tt>"fooBarBaz"</tt> will generate a name of <tt>"Foo bar baz"</tt>. If a name other
     * than the property name is desired, or if generating a sentence-case string would be inappropriate, use
     * {@link #PropertyItem(Property, String)} that lets the name be directly specified.
     *
     * @param property the property the item represents
     */
    public PropertyItem(Property<T> property) {
      this(property, camelCaseToSentence(property.getName()));
    }

    /**
     * Creates a new PropertyItem from the given property and with the given name.
     *
     * @param property the property the item represents
     * @param name     the name of the item to display in the property sheet
     */
    public PropertyItem(Property<T> property, String name) {
      this.property = property;
      this.name = name;
    }

    /**
     * Converts a "CamelCase" string to "Sentence case". This is implemented by replacing every upper-case character
     * (except for the first one, if it is upper-case) with a space character (<tt>' '</tt>) and that character's
     * lower-case representation.
     */
    private static String camelCaseToSentence(String camel) {
      if (camel == null) {
        return null;
      } else if (camel.isEmpty()) {
        return "";
      }
      final char[] chars = camel.toCharArray();
      StringBuilder builder = new StringBuilder();
      builder.append(Character.toUpperCase(chars[0]));
      for (int i = 1; i < chars.length; i++) {
        char next = chars[i];
        if (Character.isUpperCase(next)) {
          builder.append(' ').append(Character.toLowerCase(next));
        } else {
          builder.append(next);
        }
      }
      return builder.toString();
    }

    @Override
    public Class<?> getType() {
      return property.getValue().getClass();
    }

    @Override
    public String getCategory() {
      return "Ungrouped";
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public Object getValue() {
      return property.getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
      if (getType().isInstance(value)) {
        property.setValue((T) value);
      } else {
        throw new IllegalArgumentException(
            String.format("Cannot set value to %s (expected a %s, but was a %s)",
                value, getType().getName(), value.getClass().getName()));
      }
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
      return Optional.of(property);
    }

  }

  @Override
  protected Skin<?> createDefaultSkin() {
    // Very similar to PropertySheetSkin, but changes the Category view to put everything in a single pane with headers
    // for each category instead of an accordion view (which is very MEH in our context)
    return new ExtendedPropertySheetSkin(this);
  }

  private abstract static class DebouncedPropertyEditor<T, C extends Control> extends AbstractPropertyEditor<T, C> {

    private static final Duration DEFAULT_DEBOUNCE_DELAY = Duration.ofMillis(250);

    public DebouncedPropertyEditor(Item property, C control) {
      super(property, control);
      property.getObservableValue()
          .filter(v -> v instanceof FlushableProperty)
          .map(v -> (FlushableProperty<? super T>) v)
          .ifPresent(flushable -> {
            Debouncer debouncer = new Debouncer(() -> FxUtils.runOnFxThread(flushable::flush), DEFAULT_DEBOUNCE_DELAY);
            getObservableValue().addListener((__, oldValue, newValue) -> debouncer.run());
          });
    }
  }

  /**
   * A property editor for numbers. We use this instead of the one bundled with ControlsFX because
   * their implementation is bad.
   */
  private static class NumberPropertyEditor extends DebouncedPropertyEditor<Double, NumberField> {

    NumberPropertyEditor(Item item) {
      super(item, new NumberField(((Number) item.getValue()).doubleValue()));
    }

    @Override
    protected ObservableValue<Double> getObservableValue() {
      return getEditor().numberProperty();
    }

    @Override
    public void setValue(Double value) {
      getEditor().setNumber(value);
    }

  }

  private static class IntegerPropertyEditor extends DebouncedPropertyEditor<Integer, IntegerField> {

    IntegerPropertyEditor(Item item) {
      super(item, new IntegerField((Integer) item.getValue()));
    }

    @Override
    protected ObservableValue<Integer> getObservableValue() {
      return getEditor().numberProperty();
    }

    @Override
    public void setValue(Integer value) {
      getEditor().setNumber(value);
    }
  }


  private static class TextPropertyEditor extends DebouncedPropertyEditor<String, TextField> {

    TextPropertyEditor(Item item) {
      super(item, new TextField((String) item.getValue()));
    }

    @Override
    protected ObservableValue<String> getObservableValue() {
      return getEditor().textProperty();
    }

    @Override
    public void setValue(String value) {
      getEditor().setText(value);
    }

  }

  private static class ToggleSwitchEditor extends AbstractPropertyEditor<Boolean, ToggleSwitch> {

    ToggleSwitchEditor(Item item) {
      super(item, new ToggleSwitch());
    }

    @Override
    protected ObservableValue<Boolean> getObservableValue() {
      return getEditor().selectedProperty();
    }

    @Override
    public void setValue(Boolean value) {
      getEditor().setSelected(value);
    }

  }

  private static class ThemePropertyEditor extends AbstractPropertyEditor<Theme, ComboBox<Theme>> {

    private static class ThemeStringConverter extends StringConverter<Theme> {

      @Override
      public String toString(Theme object) {
        return object.getName();
      }

      @Override
      public Theme fromString(String string) {
        return Themes.getDefault().forName(string);
      }
    }

    ThemePropertyEditor(PropertySheet.Item property) {
      super(property, new ComboBox<>());
      getEditor().setItems(Themes.getDefault().getThemes());
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

  /**
   * An item wrapping a single {@link Setting}.
   */
  private static class SettingsItem implements Item {
    private final Setting<?> setting;
    private final Group group;

    public SettingsItem(Group group, Setting<?> setting) {
      this.setting = setting;
      this.group = group;
    }

    @Override
    public Class<?> getType() {
      Class<?> type = setting.getType();
      return type == null ? setting.getProperty().getValue().getClass() : type;
    }

    @Override
    public String getCategory() {
      return group.getName();
    }

    @Override
    public String getName() {
      return setting.getName();
    }

    @Override
    public String getDescription() {
      return setting.getDescription();
    }

    @Override
    public Object getValue() {
      return setting.getProperty().getValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
      ((Setting) setting).setValue(value);
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
      return Optional.of(setting.getProperty());
    }
  }
}
