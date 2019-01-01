package edu.wpi.first.shuffleboard.api;

/**
 * Parses arbitrary input and returns the result. This is used to set the values of
 * {@link edu.wpi.first.shuffleboard.api.widget.Component Component} settings provided by a remote definition (such as
 * a FRC robot program).
 *
 * @param <T> the type of values this parser provides
 */
public interface PropertyParser<T> {

  /**
   * Creates a new property parser for an enum type. Valid inputs are {@code E}, {@code String} (matching the enum
   * constant name), and {@code Integer} (enum ordinal).
   *
   * @param type the enum type to create a parser for
   * @param <E>  the type of the enum class
   *
   * @return a parser that outputs constants from the given enum type
   */
  static <E extends Enum<E>> PropertyParser<E> forEnum(Class<E> type) {
    if (!type.isEnum()) {
      throw new IllegalArgumentException("Not an enum type: " + type);
    }
    return new PropertyParser<>() {
      @Override
      public Class<E> outputType() {
        return type;
      }

      @Override
      public boolean canParse(Object input) {
        return type.isInstance(input)
            || input instanceof String
            || input instanceof Integer;
      }

      @Override
      public E parse(Object input) {
        if (type.isInstance(input)) {
          return (E) input;
        }
        E[] values = type.getEnumConstants();
        if (input instanceof String) {
          for (E value : values) {
            if (value.name().equalsIgnoreCase((String) input)) {
              return value;
            }
          }
        } else if (input instanceof Integer) {
          return values[(Integer) input];
        }
        throw new IllegalArgumentException("Unsupported input: " + input);
      }
    };
  }

  /**
   * Gets the type of the output of the parser.
   */
  Class<T> outputType();

  /**
   * Checks if the given input is supported by this parser.
   *
   * @param input the input to check
   *
   * @return true if the input can be {@link #parse(Object) parsed}, false if not
   */
  boolean canParse(Object input);

  /**
   * Parses the given input and returns the result.
   *
   * @param input the value to parse
   *
   * @return the parsed value
   */
  T parse(Object input);
}
