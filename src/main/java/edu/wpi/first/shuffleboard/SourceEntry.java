package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.sources.DataSource;
import java.io.Serializable;

/** A representation of a source. */
public interface SourceEntry extends Serializable {

  /** The name of the source. */
  String getName();

  /** The type of the source this entry is associated with. */
  Class<? extends DataSource> getType();
}
