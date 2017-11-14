package edu.wpi.first.shuffleboard.api.widget;

/**
 * A checked exception that can be thrown when a component cannot be instantiated for any reason.
 */
public class ComponentInstantiationException extends RuntimeException {

  private final ComponentType<?> componentType;

  public ComponentInstantiationException(ComponentType<?> componentType) {
    super(generateMessage(componentType));
    this.componentType = componentType;
  }

  public ComponentInstantiationException(ComponentType<?> componentType, Throwable cause) {
    super(generateMessage(componentType), cause);
    this.componentType = componentType;
  }

  public final ComponentType<?> getComponentType() {
    return componentType;
  }

  private static String generateMessage(ComponentType<?> componentType) {
    return String.format(
        "Could not instantiate %s (type: %s)",
        componentType.getName(),
        componentType.getType().getName()
    );
  }

}
