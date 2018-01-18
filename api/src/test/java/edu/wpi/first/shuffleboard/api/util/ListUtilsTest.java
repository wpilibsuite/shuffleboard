package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static edu.wpi.first.shuffleboard.api.util.ListUtils.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ListUtilsTest {

  private static final String foo = "foo";
  private static final String bar = "bar";
  private static final Supplier<String> barSupplier = () -> bar;
  private static final Supplier<String> nullSupplier = () -> null;

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

  @Test
  public void testJoiningNoSeparatorsNoBookends() {
    List<String> list = Arrays.asList(foo, bar);
    List<String> result = list.stream().collect(joining(nullSupplier, nullSupplier, nullSupplier));
    assertEquals(list, result);
  }

  @Test
  public void testJoiningNoSeparatorsWithBookends() {
    List<String> result = Stream.of(foo, foo, foo)
        .collect(joining(barSupplier, nullSupplier, barSupplier));
    assertEquals(Arrays.asList(bar, foo, foo, foo, bar), result);
  }

  @Test
  public void testJoining1NoBookends() {
    assertEquals(
        Collections.singletonList(foo),
        Stream.of(foo).collect(joining(barSupplier))
    );
  }

  @Test
  public void testJoining1WithBookends() {
    assertEquals(
        Arrays.asList(bar, foo, bar),
        Stream.of(foo).collect(joining(barSupplier, barSupplier, barSupplier))
    );
  }

  @Test
  public void testJoining2() {
    List<? extends String> list = Stream.of(foo, foo)
        .collect(joining(barSupplier));
    assertEquals(Arrays.asList(foo, bar, foo), list);
  }

  @Test
  public void testJoining3() {
    List<? extends String> list = Stream.of(foo, foo, foo)
        .collect(joining(barSupplier));
    assertEquals(Arrays.asList(foo, bar, foo, bar, foo), list);
  }

  @Test
  public void testJoining2Bookend() {
    List<? extends String> list = Stream.of(foo, foo)
        .collect(joining(barSupplier, barSupplier, barSupplier));
    assertEquals(Arrays.asList(bar, foo, bar, foo, bar), list);
  }

  @Test
  public void testJoining3Bookend() {
    List<? extends String> list = Stream.of(foo, foo, foo)
        .collect(joining(barSupplier, barSupplier, barSupplier));
    assertEquals(Arrays.asList(bar, foo, bar, foo, bar, foo, bar), list);
  }

}
