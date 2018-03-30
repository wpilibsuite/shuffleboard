package edu.wpi.first.shuffleboard.plugin.base.widget;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PrimitiveDoubleArrayListTest {

  @Test
  public void testEmpty() {
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    assertAll(
        () -> assertEquals(0, list.size(), "Size is not zero"),
        () -> assertTrue(list.isEmpty(), "List is not empty")
    );
  }

  @Test
  public void testAdd() {
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    list.add(12.34);
    list.add(Math.PI);
    assertAll(
        () -> assertEquals(2, list.size(), "Size is not two"),
        () -> assertFalse(list.isEmpty(), "List should not be empty"),
        () -> assertEquals(12.34, list.get(0)),
        () -> assertEquals(Math.PI, list.get(1))
    );
  }

  @Test
  public void testAddALot() {
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    for (int i = 0; i < 256; i++) {
      list.add(i);
    }
    assertEquals(256, list.size());
    assertAll(IntStream.range(0, 256).mapToObj(i -> (Executable) () -> assertEquals((double) i, list.get(i))));
  }

  @Test
  public void testToArray() {
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    for (int i = 0; i < 256; i++) {
      list.add(i);
    }
    double[] array = list.toArray();
    assertEquals(256, array.length);
    assertAll(IntStream.range(0, 256).mapToObj(i -> (Executable) () -> assertEquals(list.get(i), array[i])));
  }

  @Test
  public void testStream() {
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    for (int i = 0; i < 256; i++) {
      list.add(i);
    }
    assertAll(list.stream()
        .mapToObj(d -> (Executable) () -> assertEquals(d, list.get((int) d))));
  }

  @Test
  public void testEmptyIterator() {
    int count = 0;
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    for (double value : list) {
      count++;
    }
    assertEquals(0, count);
  }

  @Test
  public void testPopulatedIterator() {
    PrimitiveDoubleArrayList list = new PrimitiveDoubleArrayList();
    for (int i = 0; i < 256; i++) {
      list.add(i);
    }
    int count = 0;
    for (double value : list) {
      count++;
    }
    assertEquals(count, list.size());
  }

}
