package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * A ComponentType is a Java object that encapsulates a single "variety" of component that the
 * user could instantiate.
 *
 * <p>It can be thought of as a wrapper around Class&lt;? extends Component&gt; with a
 * more useful API and additional metadata.
 */
public interface ComponentType<C extends Component> {

  Class<C> getType();

  /**
   * Get the name of the component (ex: "Number Slider").
   */
  String getName();

  /**
   * Gets a set of data types that this component is capable of displaying.
   */
  default Set<DataType> getDataTypes() {
    return ImmutableSet.of();
  }

  /**
   * Gets a new instance of the component.
   *
   * @throws ComponentInstantiationException if there was an exception when attempting to create a new component
   */
  C get() throws ComponentInstantiationException;

}
