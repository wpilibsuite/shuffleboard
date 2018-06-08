package edu.wpi.first.shuffleboard.api.prefs;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CategoryTest {

  @Test
  public void testInvalidNames() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> Category.of(null)),
        () -> assertThrows(IllegalArgumentException.class, () -> Category.of("")),
        () -> assertThrows(IllegalArgumentException.class, () -> Category.of(" ")),
        () -> assertThrows(IllegalArgumentException.class, () -> Category.of("\n")),
        () -> assertThrows(IllegalArgumentException.class, () -> Category.of("\t"))
    );
  }

  @Test
  public void sanityDataTest() {
    Category subA = Category.of("Sub A");
    Category subB = Category.of("Sub B");
    Group groupA = Group.of("Group A");
    Group groupB = Group.of("Group B");
    Category category = Category.of("Name",
        ImmutableList.of(subA, subB),
        ImmutableList.of(groupA, groupB)
    );

    assertAll(
        () -> assertEquals("Name", category.getName(), "Name was different"),
        () -> assertEquals(ImmutableList.of(subA, subB), category.getSubcategories(), "Subcategories were different"),
        () -> assertEquals(ImmutableList.of(groupA, groupB), category.getGroups(), "Groups were different")
    );
  }

}
