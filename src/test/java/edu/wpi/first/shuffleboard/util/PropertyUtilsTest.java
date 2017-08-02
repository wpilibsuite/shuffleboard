package edu.wpi.first.shuffleboard.util;

import org.junit.Test;

import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static org.junit.Assert.*;

public class PropertyUtilsTest {

  @Test
  public void testCombineLists() {
    ObservableList<String> a = FXCollections.observableArrayList("a");
    ObservableList<String> b = FXCollections.observableArrayList("b");
    ObservableList<String> combine = PropertyUtils.combineLists(a, b);
    assertEquals(2, combine.size());
    assertEquals(a.get(0), combine.get(0));
    assertEquals(b.get(0), combine.get(1));
  }

  @Test
  public void testCombineListsListeners() {
    ObservableList<String> a = FXCollections.observableArrayList("a");
    ObservableList<String> b = FXCollections.observableArrayList("b");
    ObservableList<String> combine = PropertyUtils.combineLists(a, b);

    a.add("A");
    assertEquals(3, combine.size());
    b.add("B");
    assertEquals(4, combine.size());
    assertEquals(a.get(1), combine.get(2));
    assertEquals(b.get(1), combine.get(3));

    a.clear();
    b.clear();
    assertEquals(0, combine.size());
  }

}
