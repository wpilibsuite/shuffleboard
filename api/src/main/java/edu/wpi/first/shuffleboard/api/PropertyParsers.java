package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.geometry.Orientation;
import javafx.scene.paint.Color;

/**
 * Registry for {@link PropertyParser PropertyParsers}.
 */
public final class PropertyParsers extends Registry<PropertyParser<?>> {

  private static final PropertyParser<Orientation> ORIENTATION =
      PropertyParser.forEnum(Orientation.class);

  private static final PropertyParser<LayoutBase.LabelPosition> LABEL_POSITION =
      PropertyParser.forEnum(LayoutBase.LabelPosition.class);

  private static final PropertyParser<String> STRING = new StringPropertyParser();
  private static final PropertyParser<Number> NUMBER = new NumberPropertyParser();
  private static final PropertyParser<Boolean> BOOLEAN = new BooleanPropertyParser();
  private static final PropertyParser<Color> COLOR = new ColorPropertyParser();

  private static final PropertyParsers defaultInstance = new PropertyParsers();

  public PropertyParsers() {
    registerAll(BOOLEAN, COLOR, NUMBER, STRING, ORIENTATION, LABEL_POSITION);
  }

  public static PropertyParsers getDefault() {
    return defaultInstance;
  }

  @Override
  public void register(PropertyParser<?> item) {
    if (getItems().stream().anyMatch(item::equals)) {
      throw new IllegalArgumentException("Parser " + item + " is already registered");
    }
    if (getItems().stream().map(PropertyParser::outputType).anyMatch(item.outputType()::equals)) {
      throw new IllegalArgumentException(
          "A parser is already registered with the same output type: " + item.outputType());
    }
    addItem(item);
  }

  @Override
  public void unregister(PropertyParser<?> item) {
    removeItem(item);
  }

  /**
   * Parses the given input as a value of the given output type.
   *
   * @param input      the value to parse
   * @param outputType the type of the value to parse as
   * @param <T>        the type of the parsed result
   *
   * @return the parse result
   *
   * @throws IllegalStateException if there are multiple registered parsers for type {@code T}
   */
  public <T> Optional<T> parse(Object input, Class<T> outputType) {
    Set<T> possibilities = getItems()
        .stream()
        .filter(p -> p.outputType().isAssignableFrom(outputType))
        .filter(p -> p.canParse(input))
        .map(p -> (PropertyParser<T>) p)
        .map(p -> p.parse(input))
        .collect(Collectors.toSet());
    if (possibilities.isEmpty()) {
      return Optional.empty();
    }
    if (possibilities.size() > 1) {
      throw new IllegalStateException(
          "Multiple parsers for " + input + " supporting output type " + outputType.getSimpleName());
    }
    return Optional.of(possibilities.iterator().next());
  }

  private static final class StringPropertyParser implements PropertyParser<String> {
    @Override
    public Class<String> outputType() {
      return String.class;
    }

    @Override
    public boolean canParse(Object input) {
      return input != null;
    }

    @Override
    public String parse(Object input) {
      return input.toString();
    }
  }

  private static final class NumberPropertyParser implements PropertyParser<Number> {
    @Override
    public Class<Number> outputType() {
      return Number.class;
    }

    @Override
    public boolean canParse(Object input) {
      return input instanceof Number;
    }

    @Override
    public Number parse(Object input) {
      return (Number) input;
    }
  }

  private static final class BooleanPropertyParser implements PropertyParser<Boolean> {
    @Override
    public Class<Boolean> outputType() {
      return Boolean.class;
    }

    @Override
    public boolean canParse(Object input) {
      return input instanceof Boolean;
    }

    @Override
    public Boolean parse(Object input) {
      return (Boolean) input;
    }
  }

  private static final class ColorPropertyParser implements PropertyParser<Color> {
    @Override
    public Class<Color> outputType() {
      return Color.class;
    }

    @Override
    public boolean canParse(Object input) {
      return input instanceof Color
          || input instanceof String
          || input instanceof Number;
    }

    @Override
    @SuppressWarnings("LocalVariableName") // r, g, b, a for color channels are OK names
    public Color parse(Object input) {
      if (input instanceof Color) {
        return (Color) input;
      } else if (input instanceof String) {
        return Color.web((String) input);
      } else if (input instanceof Number) {
        int rgba = ((Number) input).intValue();
        int r = (rgba >> 24) & 0xFF;
        int g = (rgba >> 16) & 0xFF;
        int b = (rgba >> 8) & 0xFF;
        int a = rgba & 0xFF;
        return Color.rgb(r, g, b, a / 255.0);
      } else {
        throw new IllegalArgumentException("Unsupported input: " + input);
      }
    }
  }
}
