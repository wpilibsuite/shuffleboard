package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyUtilsTest extends UtilityClassTest<PropertyUtils> {

  @Test
  public void testBindBidirectionalWithConverterNullFirstInitialValue() {
    // given
    Property<String> str = new SimpleStringProperty(null);
    Property<Number> num = new SimpleDoubleProperty(0.0);

    // when
    PropertyUtils.bindBidirectionalWithConverter(str, num, Double::parseDouble, Object::toString);

    // then
    assertAll(
        () -> assertEquals("0.0", str.getValue(), "String was not set correctly"),
        () -> assertEquals(0.0, num.getValue().doubleValue(), "Number should not have changed")
    );
  }

  @Test
  public void testBindBidirectionalWithConverterNullSecondInitialValue() {
    // given
    Property<String> str = new SimpleStringProperty("41");
    Property<Number> num = new SimpleObjectProperty<>(null);

    // when
    PropertyUtils.bindBidirectionalWithConverter(str, num, Double::parseDouble, Object::toString);

    // then
    assertAll(
        () -> assertEquals(null, str.getValue(), "String should not have changed"),
        () -> assertEquals(null, num.getValue(), "Binding target should not have changed")
    );
  }

  @Test
  public void testBindBidirectionalWithConverter() {
    // given
    Property<String> str = new SimpleStringProperty("-42");
    Property<Number> num = new SimpleDoubleProperty(1.23);

    // when
    PropertyUtils.bindBidirectionalWithConverter(str, num, Double::parseDouble, Object::toString);

    // then (initial conditions)
    assertAll(
        () -> assertEquals("1.23", str.getValue(), "String was not set correctly"),
        () -> assertEquals(1.23, num.getValue().doubleValue(), "Binding target should not have changed")
    );

    // when changing one value
    str.setValue("89");
    // then
    assertEquals(89, num.getValue().doubleValue(), "Number was not set correctly");

    // when changing the other value
    num.setValue(10.01);
    // then
    assertEquals("10.01", str.getValue(), "String was not set correctly");
  }

  @Test
  @SuppressWarnings("LocalVariableName")
  public void testCombineLists() {
    ObservableList<String> a = FXCollections.observableArrayList("a");
    ObservableList<String> b = FXCollections.observableArrayList("b");
    ObservableList<String> combine = PropertyUtils.combineLists(a, b);
    assertEquals(2, combine.size());
    assertEquals(a.get(0), combine.get(0));
    assertEquals(b.get(0), combine.get(1));
  }

  @Test
  @SuppressWarnings("LocalVariableName")
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
