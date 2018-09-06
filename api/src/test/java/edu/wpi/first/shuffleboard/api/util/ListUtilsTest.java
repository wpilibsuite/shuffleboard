package edu.wpi.first.shuffleboard.api.util;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static edu.wpi.first.shuffleboard.api.util.ListUtils.joining;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListUtilsTest extends UtilityClassTest<ListUtils> {

  private static final String foo = "foo";
  private static final String bar = "bar";
  private static final Supplier<String> barSupplier = () -> bar;
  private static final Supplier<String> nullSupplier = () -> null;

  @Test
  public void testFirstIndexOfNulls() {
    assertAll(
        () -> assertThrows(NullPointerException.class, () -> ListUtils.firstIndexOf(null, x -> true)),
        () -> assertThrows(NullPointerException.class, () -> ListUtils.firstIndexOf(new ArrayList<>(), null))
    );
  }

  @Test
  public void testFirstIndexOf() {
    List<String> list = new ArrayList<>();
    list.add("1");
    list.add("12");
    list.add("123");
    assertAll(
        () -> assertEquals(-1, ListUtils.firstIndexOf(list, str -> str.startsWith("0"))), // NOPMD
        () -> assertEquals(0, ListUtils.firstIndexOf(list, str -> str.equals("1"))),      // NOPMD
        () -> assertEquals(1, ListUtils.firstIndexOf(list, str -> str.length() == 2)),
        () -> assertEquals(2, ListUtils.firstIndexOf(list, str -> str.endsWith("3")))
    );
  }

  @Test
  public void testAddIfNotPresentNoIndex() {
    List<String> list = new ArrayList<>();
    list.add(foo);
    assertAll(
        () -> assertFalse(ListUtils.addIfNotPresent(list, foo)),
        () -> assertTrue(ListUtils.addIfNotPresent(list, bar)),
        () -> assertFalse(ListUtils.addIfNotPresent(list, bar)),
        () -> assertEquals(2, list.size())
    );
  }

  @Test
  public void testAddIfNotPresentToIndex() {
    List<String> list = new ArrayList<>();
    list.add(foo);
    assertAll(
        () -> assertTrue(ListUtils.addIfNotPresent(list, 0, bar)),
        () -> assertFalse(ListUtils.addIfNotPresent(list, 0, bar)),
        () -> assertEquals(0, list.indexOf(bar)),
        () -> assertEquals(1, list.indexOf(foo)),
        () -> assertEquals(2, list.size())
    );
  }

  @Test
  public void testToImmutableList() {
    List<String> list = Arrays.asList(
        foo,
        bar,
        "baz",
        bar,
        foo,
        "abcd"
    );
    // Can't get perfect coverage here, since code coverage doesn't cover the lambdas/method references
    ImmutableList<String> immutableList = list.stream().collect(ListUtils.toImmutableList());
    assertEquals(list, immutableList);
  }

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
