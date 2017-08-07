package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import java.util.Set;

/**
 * A type of widget that has its name and data types set with a {@link Description} annotation on the class. Widget
 * classes that are also FXML controllers should specify the FXML file with a {@link ParametrizedController} annotation.
 */
public abstract class AnnotatedWidget extends AbstractWidget {

  private Description description = getClass().getAnnotation(Description.class);

  @Override
  public final String getName() {
    return getDescription().name();
  }

  @Override
  public final Set<DataType> getDataTypes() {
    return DataTypes.forTypes(getDescription().dataTypes());
  }

  private Description getDescription() {
    if (description == null) {
      description = getClass().getAnnotation(Description.class);
    }
    return description;
  }

}
