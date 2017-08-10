package edu.wpi.first.shuffleboard.api.sources;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * A representation of a source.
 */
public interface SourceEntry extends Serializable, Supplier<DataSource> {

  /**
   * The name of the source.
   */
  String getName();

  /**
   * The type of the source this entry is associated with.
   */
  Class<? extends DataSource> getType();

}
