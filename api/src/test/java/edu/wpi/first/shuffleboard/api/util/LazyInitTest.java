package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class LazyInitTest {

  @Test
  public void testNullInitializer() {
    assertThrows(NullPointerException.class, () -> LazyInit.of(null), "Null initializer should throw");
  }

  @Test
  public void testCachedValue() {
    LazyInit<Object> lazyInit = LazyInit.of(Object::new);
    Object value = lazyInit.get();
    assertSame(value, lazyInit.get(), "The initial value should have been cached");
  }

  @Test
  public void testInitializerThrows() {
    LazyInit<Object> throwing = LazyInit.of(() -> {
      throw new UnsupportedOperationException();
    });
    try {
      throwing.get();
      fail("No exception was thrown");
    } catch (RuntimeException e) {
      assertNotNull(e.getCause());
      assertEquals(UnsupportedOperationException.class, e.getCause().getClass());
    }
  }

}
