package edu.wpi.first.shuffleboard.api.data;

import com.google.common.collect.Lists;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.wpi.first.shuffleboard.api.data.DataTypes.closestTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DataTypesTest {

  class A {}

  class B extends A {}

  class C extends B {}

  class D extends C {}

  @Test
  public void testClosestTo() {
    final List<Class<?>> classes = Lists.newArrayList(C.class, B.class, D.class, A.class);

    classes.sort(closestTo(Object.class));
    assertEquals(Lists.newArrayList(D.class, C.class, B.class, A.class), classes);

    classes.sort(closestTo(D.class));
    assertEquals(Lists.newArrayList(A.class, B.class, C.class, D.class), classes);
  }

}
