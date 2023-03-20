package edu.wpi.first.shuffleboard.app.sources;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.plugin.base.data.types.GyroType;
import org.junit.jupiter.api.Test;

class SourceComparisonTest {
  @Test
  void notEqualsToDestroyed() {
    var dummySource = new DummySource<>(GyroType.Instance, GyroType.Instance.getDefaultValue());
    var destroyedSource = new DestroyedSource<>(dummySource);
    assertAll(() -> assertNotEquals(destroyedSource, dummySource, "destroyed in lhs"),
            () -> assertNotEquals(dummySource, destroyedSource, "destroyed in rhs"));
  }

  @Test
  void reflexive() {
    var dummySource = new DummySource<>(GyroType.Instance, GyroType.Instance.getDefaultValue());
    assertEquals(dummySource, dummySource);
  }

  @Test
  void symmetrical() {
    var dummySource1 = new DummySource<>(GyroType.Instance, GyroType.Instance.getDefaultValue());
    var dummySource2 = new DummySource<>(GyroType.Instance, GyroType.Instance.getDefaultValue());
    assertEquals(dummySource1, dummySource2);
    assertEquals(dummySource2, dummySource1);
  }
}
