package edu.wpi.first.shuffleboard.api.widget;

import java.util.function.Supplier;

/**
 * A ComponentType is a Java object that encapsulates a single "variety" of component that the
 * user could instantiate.
 *
 * <p>It can be thought of as a wrapper around Class&lt;? extends Component&gt; with a
 * more useful API and additional metadata.
 */
public interface ComponentType<C extends Component> extends Supplier<C> {
  /**
   * Get the name of the component (ex: "Number Slider").
   */
  String getName();
}
