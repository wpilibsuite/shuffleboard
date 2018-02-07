package edu.wpi.first.shuffleboard.plugin.powerup;

import edu.wpi.first.shuffleboard.plugin.base.data.fms.Alliance;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.FmsInfo;

/**
 * Contains the configuration of the 2018 field elements.
 */
public final class FieldConfiguration {

  private static final FieldConfiguration EMPTY =
      new FieldConfiguration(Configuration.UNKNOWN, Configuration.UNKNOWN, Configuration.UNKNOWN);

  private final Configuration nearSwitch;
  private final Configuration scale;
  private final Configuration farSwitch;

  /**
   * Creates a field configuration.
   *
   * @param nearSwitch the configuration of the near switch
   * @param scale      the configuration of the scale
   * @param farSwitch  the configuration of the far switch
   */
  public FieldConfiguration(Configuration nearSwitch, Configuration scale, Configuration farSwitch) {
    this.nearSwitch = nearSwitch;
    this.scale = scale;
    this.farSwitch = farSwitch;
  }

  /**
   * Parses FMS info to extract the field configuration. If the game-specific message is incorrectly formatted, returns
   *
   * @param info the FMS info to parse
   */
  public static FieldConfiguration parseFmsInfo(FmsInfo info) {
    Alliance alliance = info.getAlliance();
    String message = info.getGameSpecificMessage();
    if (alliance == null || message.isEmpty() || message.length() != 3) {
      return EMPTY;
    }
    return new FieldConfiguration(
        getConfig(Element.NEAR_SWITCH, alliance, message),
        getConfig(Element.SCALE, alliance, message),
        getConfig(Element.FAR_SWITCH, alliance, message)
    );
  }

  /**
   * Gets the configuration of a specific field element.
   *
   * @param element             the element to get the configuration of
   * @param alliance            the current alliance
   * @param gameSpecificMessage the game-specific message to parse the location from
   */
  static Configuration getConfig(Element element, Alliance alliance, String gameSpecificMessage) {
    if (gameSpecificMessage.length() != 3) {
      return Configuration.UNKNOWN;
    }
    char value = gameSpecificMessage.charAt(element.ordinal());
    switch (value) {
      case 'L':
        // fallthrough
      case 'l':
        switch (alliance) {
          case RED:
            return Configuration.RED_LEFT;
          case BLUE:
            return Configuration.RED_RIGHT;
          default:
            return Configuration.UNKNOWN;
        }
      case 'R':
        // fallthrough
      case 'r':
        switch (alliance) {
          case RED:
            return Configuration.RED_RIGHT;
          case BLUE:
            return Configuration.RED_LEFT;
          default:
            return Configuration.UNKNOWN;
        }
      default:
        return Configuration.UNKNOWN;
    }
  }

  /**
   * Gets the configuration of the near (this alliance's) switch.
   */
  public Configuration getNearSwitch() {
    return nearSwitch;
  }

  /**
   * Gets the configuration of the scale.
   */
  public Configuration getScale() {
    return scale;
  }

  /**
   * Gets the configuration of the far (opposing alliance's) switch.
   */
  public Configuration getFarSwitch() {
    return farSwitch;
  }

}
