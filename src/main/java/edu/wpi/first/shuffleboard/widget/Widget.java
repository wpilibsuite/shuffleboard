package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.IncompatibleSourceException;
import javafx.scene.layout.Pane;

import java.util.Set;

public interface Widget {
  /**
   * Gets the JavaFX pane used to display this widget in the UI.
   */
  Pane getView();

  /**
   * Gets the name of this widget.
   */
  String getName();

  /**
   * Gets an unmodifiable copy of this widgets supported data types.
   */
  Set<DataType> getDataTypes();

  void setSource(DataSource source) throws IncompatibleSourceException;

  DataSource<?> getSource();
}
