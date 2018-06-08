package edu.wpi.first.shuffleboard.api.prefs;

import org.junit.jupiter.api.Test;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GroupTest {
  
  private final Property<?> property = new SimpleBooleanProperty();

  @Test
  public void testInvalidNames() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Group.of(null)),
        () -> assertThrows(IllegalArgumentException.class, () -> Group.of("")),
        () -> assertThrows(IllegalArgumentException.class, () -> Group.of(" ")),
        () -> assertThrows(IllegalArgumentException.class, () -> Group.of("\n")),
        () -> assertThrows(IllegalArgumentException.class, () -> Group.of("\t"))
    );
  }

  @Test
  public void sanityDataTest() {
    Setting<?> settingA = Setting.of("A", "A", property);
    Setting<?> settingB = Setting.of("B", "B", property);
    Group group = Group.of("Name", settingA, settingB);
    assertAll(
        () -> assertEquals("Name", group.getName(), "Name was different"),
        () -> assertEquals(settingA, group.getSettings().get(0), "First setting was different"),
        () -> assertEquals(settingB, group.getSettings().get(1), "Second setting was different")
    );
  }

}
