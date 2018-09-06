package edu.wpi.first.shuffleboard.api.util;

/**
 * Utility class for working with raw bytes, bitmasks, bitshifts, et cetera.
 */
public final class BitUtils {

  private BitUtils() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  /**
   * Checks if a bitfield contains a certain flag. This is equivalent to {@code (word & flag) != 0}.
   *
   * <p>For example, {@code flagMatches(0b1100, 0b0001)} returns {@code false}, and
   * {@code flagMatches(0b1100, 0b0100)} returns {@code true}.
   *
   * @param word the bitfield to check
   * @param flag the flag to check
   *
   * @return true if the bitfield contains the given flag, false if not
   */
  public static boolean flagMatches(int word, int flag) {
    return (word & flag) != 0;
  }

  /**
   * Converts a boolean value to a specific bitflag.
   *
   * <p>For example, {@code toFlag(isEnabled, ENABLED_FLAG)} returns {@code ENABLED_FLAG} if {@code isEnabled == true},
   * and returns {@code 0} if {@code isEnabled == false}.
   *
   * @param value the value to convert
   * @param flag  the bit representing the flag
   *
   * @return the bitflag if {@code value} is {@code true}, {@code 0} if {@code value} is {@code false}
   */
  public static int toFlag(boolean value, int flag) {
    return value ? flag : 0;
  }

}
