package edu.wpi.first.shuffleboard.app.sources;

import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;

public final class NetworkTableSourceType extends SourceType {

  public static final NetworkTableSourceType INSTANCE = new NetworkTableSourceType();

  static {
    SourceTypes.register(INSTANCE);
  }

  private NetworkTableSourceType() {
    super("NetworkTable", true, "network_table://", NetworkTableSource::forKey);
  }

}
