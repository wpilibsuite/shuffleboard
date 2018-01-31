package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static edu.wpi.first.shuffleboard.api.util.BitUtils.flagMatches;
import static edu.wpi.first.shuffleboard.api.util.BitUtils.toFlag;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BitUtilsTest extends UtilityClassTest<BitUtils> {

  @ParameterizedTest(name = "Flag: {0}")
  @MethodSource("createFlagArgs")
  public void testFlagMatches(Integer flag) {
    assertAll(() -> assertTrue(flagMatches(flag, flag), "Flag should always match itself"),
        () -> assertTrue(flagMatches(0xFFFFFFFF, flag), "0xFFFFFFFF should always match a flag"),
        () -> assertFalse(flagMatches(0, flag), "0x00000000 should never match a flag"),
        () -> assertFalse(flagMatches(flag, 0), String.format("0x%08X should never match flag 0", flag))
    );
  }

  @ParameterizedTest(name = "Flag: {0}")
  @MethodSource("createFlagArgs")
  public void testBooleanToFlag(Integer flag) {
    assertEquals(flag.intValue(), toFlag(true, flag));
    assertEquals(0, toFlag(false, flag));
  }

  private static Stream<Arguments> createFlagArgs() {
    return IntStream.range(0, 32)
        .map(i -> 1 << i)
        .mapToObj(Arguments::of);
  }

}
