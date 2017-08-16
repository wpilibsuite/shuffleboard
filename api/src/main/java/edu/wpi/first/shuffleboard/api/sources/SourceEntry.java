package edu.wpi.first.shuffleboard.api.sources;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * A representation of a source used for the drag-and-drop of sources, as well as previewing them in the application.
 * Sources are previewed in a {@link javafx.scene.control.TreeTableView TreeTableView} with two columns: "Name" and
 * "Value". The values of the "Name" column are displayed using {@link #getNamePreview()}; the "Value" column uses
 * {@link #getValue}.
 */
public interface SourceEntry extends Serializable, Supplier<DataSource> {

  /**
   * The name of a source corresponding to this entry.
   */
  String getName();

  /**
   * The string to use to display the name of the source. Defaults to {@link #getName()}, but may be overriden for
   * entries for sources with a nested structure, such as NetworkTables.
   */
  default String getViewName() {
    return getName();
  }

  /**
   * The value of a source corresponding to this entry.
   */
  Object getValue();

  /**
   * Gets an object used to display the value of the source this entry represents. Implementers are encouraged to
   * sharpen the return type
   */
  Object getValueView();

}
