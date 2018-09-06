package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import java.util.Set;

/**
 * A type of widget that has its name and data types set with a {@link Description} annotation on the class. Widget
 * classes that are also FXML controllers should specify the FXML file with a {@link ParametrizedController} annotation.
 */
public interface AnnotatedWidget extends Widget {

  @Override
  default String getName() {
    return getDescription().name();
  }

  @Override
  default Set<DataType> getDataTypes() {
    return DataTypes.getDefault().forJavaTypes(getDescription().dataTypes());
  }

  default Description getDescription() {
    return getClass().getAnnotation(Description.class);
  }

}
