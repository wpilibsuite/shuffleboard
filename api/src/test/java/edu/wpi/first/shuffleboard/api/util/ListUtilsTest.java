package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListUtilsTest {

  private static final String foo = "foo";
  private static final String bar = "bar";

  @Test
  public void testReplaceFirst() {
    List<String> list = Arrays.asList(foo, foo, bar);
    ListUtils.replaceIn(list)
        .replace(foo)
        .first()
        .with(bar);
    assertEquals(Arrays.asList(bar, foo, bar), list);
  }

  @Test
  public void testReplaceAll() {
    List<String> list = Arrays.asList(foo, foo, bar);
    ListUtils.replaceIn(list)
        .replace(foo)
        .all()
        .with(bar);
    assertEquals(Arrays.asList(bar, bar, bar), list);
  }

  @Test
  public void testThrowsWhenNoSearchValueSet() {
    assertThrows(IllegalStateException.class, () -> ListUtils.replaceIn(new ArrayList<>()).with(() -> null));
  }

}
