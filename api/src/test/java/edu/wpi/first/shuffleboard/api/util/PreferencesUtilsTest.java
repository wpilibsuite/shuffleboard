package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreferencesUtilsTest {

  private Preferences preferences;

  @BeforeEach
  public void setup() {
    preferences = new MockPreferences();
  }

  @Test
  public void testConstructorThrowsException() throws NoSuchMethodException {
    Constructor<PreferencesUtils> constructor = PreferencesUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);

    assertThrows(InvocationTargetException.class, constructor::newInstance,
        "Invoking the constructor should throw an exception");
  }

  ////// SAVING

  @Test
  public void testSaveThrowsWithNullValue() {
    Property<?> property = new SimpleObjectProperty<>(null);

    assertThrows(IllegalArgumentException.class, () -> PreferencesUtils.save(property, preferences, null));
  }

  @Test
  public void testSaveInt() {
    // given
    String name = "int";
    int value = Integer.MAX_VALUE;
    IntegerProperty property = new SimpleIntegerProperty(null, name, value);

    // when
    PreferencesUtils.save(property, preferences);

    // then
    int saved = preferences.getInt(name, -1);
    assertEquals(value, saved);
  }

  @Test
  public void testSaveLong() {
    // given
    String name = "long";
    long value = Long.MAX_VALUE;
    LongProperty property = new SimpleLongProperty(null, name, value);

    // when
    PreferencesUtils.save(property, preferences);

    // then
    long saved = preferences.getLong(name, -1);
    assertEquals(value, saved);
  }

  @Test
  public void testSaveDouble() {
    // given
    String name = "double";
    double value = Double.MAX_VALUE;
    DoubleProperty property = new SimpleDoubleProperty(null, name, value);

    // when
    PreferencesUtils.save(property, preferences);

    // then
    double saved = preferences.getDouble(name, -1);
    assertEquals(value, saved);
  }

  @Test
  public void testSaveBoolean() {
    // given
    String name = "boolean";
    boolean value = true;
    BooleanProperty property = new SimpleBooleanProperty(null, name, value);

    // when
    PreferencesUtils.save(property, preferences);

    // then
    boolean saved = preferences.getBoolean(name, !value);
    assertEquals(value, saved);
  }

  @Test
  public void testSaveString() {
    // given
    String name = "string";
    String value = "foobar";
    StringProperty property = new SimpleStringProperty(null, name, value);

    // when
    PreferencesUtils.save(property, preferences);

    // then
    String saved = preferences.get(name, null);
    assertEquals(value, saved);
  }

  ///// READING

  @Test
  public void testReadInt() {
    // given
    String name = "int";
    int value = Integer.MIN_VALUE;
    IntegerProperty property = new SimpleIntegerProperty(null, name, -value);

    // when
    preferences.putInt(name, value);

    // then
    PreferencesUtils.read(property, preferences);
    assertEquals(property.getValue().intValue(), value);
  }

  @Test
  public void testReadLong() {
    // given
    String name = "long";
    long value = Long.MIN_VALUE;
    LongProperty property = new SimpleLongProperty(null, name, -value);

    // when
    preferences.putLong(name, value);

    // then
    PreferencesUtils.read(property, preferences);
    assertEquals(property.getValue().longValue(), value);
  }

  @Test
  public void testReadDouble() {
    // given
    String name = "double";
    double value = Double.MIN_VALUE;
    DoubleProperty property = new SimpleDoubleProperty(null, name, -value);

    // when
    preferences.putDouble(name, value);

    // then
    PreferencesUtils.read(property, preferences);
    assertEquals(property.getValue().doubleValue(), value);
  }

  @Test
  public void testReadBoolean() {
    // given
    String name = "boolean";
    boolean value = true;
    BooleanProperty property = new SimpleBooleanProperty(null, name, !value);
    preferences.putBoolean(name, value);

    // when
    PreferencesUtils.read(property, preferences);

    // then
    assertEquals(property.getValue(), value);
  }

  @Test
  public void testReadString() {
    // given
    String name = "string";
    String value = "foo";
    StringProperty property = new SimpleStringProperty(null, name, "bar");
    preferences.put(name, value);

    // when
    PreferencesUtils.read(property, preferences);

    // then
    assertEquals(property.getValue(), value);
  }

  @Test
  public void testReadWhenSavedValueIsWrongType() {
    // given
    int initialValue = 10;
    IntegerProperty property = new SimpleIntegerProperty(null, "value", initialValue);
    preferences.putBoolean("value", false);

    // when
    PreferencesUtils.read(property, preferences);

    // then
    assertEquals(initialValue, property.getValue().intValue());
  }

  @Test
  public void testReadWhenNoValuePresent() {
    // given
    double initialValue = 123.456;
    DoubleProperty property = new SimpleDoubleProperty(null, "foo", initialValue);

    // when
    PreferencesUtils.read(property, preferences);

    // then
    assertEquals(initialValue, property.getValue().doubleValue());
  }

  /**
   * A mock preferences class that stores values in a {@link Map Map&lt;String, String&gt;}.
   * This cannot have child preference nodes.
   */
  public static class MockPreferences extends AbstractPreferences {

    private final Map<String, String> map = new HashMap<>();

    public MockPreferences() {
      super(null, "");
    }

    @Override
    protected void putSpi(String key, String value) {
      map.put(key, value);
    }

    @Override
    protected String getSpi(String key) {
      return map.get(key);
    }

    @Override
    protected void removeSpi(String key) {
      map.remove(key);
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
      // no child nodes
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
      return map.keySet().toArray(new String[map.keySet().size()]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
      return new String[0]; // no children
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
      return null;
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
      // nothing to sync with
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
      // nothing to flush to
    }
  }
}