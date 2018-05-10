package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    String message = "Exception message";
    LazyInit<Object> throwing = LazyInit.of(() -> {
      throw new UnsupportedOperationException(message);
    });
    RuntimeException thrown = assertThrows(RuntimeException.class, throwing::get);
    Throwable cause = thrown.getCause();
    assertNotNull(cause);
    assertAll(
        () -> assertEquals(UnsupportedOperationException.class, cause.getClass()),
        () -> assertEquals(message, cause.getMessage())
    );
  }

}
