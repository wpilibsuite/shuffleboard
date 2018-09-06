package edu.wpi.first.shuffleboard.api.prefs;

import org.junit.jupiter.api.Test;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettingTest {

  private final Property<?> property = new SimpleBooleanProperty();

  @Test
  public void testInvalidNames() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Setting.of(null, property)),
        () -> assertThrows(IllegalArgumentException.class, () -> Setting.of("", property)),
        () -> assertThrows(IllegalArgumentException.class, () -> Setting.of(" ", property)),
        () -> assertThrows(IllegalArgumentException.class, () -> Setting.of("\n", property)),
        () -> assertThrows(IllegalArgumentException.class, () -> Setting.of("\t", property))
    );
  }

  @Test
  public void testNullProperty() {
    assertThrows(NullPointerException.class, () -> Setting.of("Name", null));
  }

  @Test
  public void sanityDataTest() {
    Setting<?> setting = Setting.of("Name", "Description", property);
    assertAll(
        () -> assertEquals("Name", setting.getName(), "Name was different"),
        () -> assertEquals("Description", setting.getDescription(), "Description was different"),
        () -> assertEquals(property, setting.getProperty(), "Property was different")
    );
  }

}
