package edu.wpi.first.shuffleboard.plugin.base.data.fms;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;

/**
 * Contains information sent by the FMS to the RoboRIO.
 */
public final class FmsInfo extends ComplexData<FmsInfo> {

  private static final String GAME_SPECIFIC_MESSAGE = "GameSpecificMessage";
  private static final String EVENT_NAME = "EventName";
  private static final String MATCH_NUMBER = "MatchNumber";
  private static final String REPLAY_NUMBER = "ReplayNumber";
  private static final String MATCH_TYPE = "MatchType";
  private static final String IS_RED_ALLIANCE = "IsRedAlliance";
  private static final String STATION_NUMBER = "StationNumber";
  private static final String FMS_CONTROL_DATA = "FMSControlData";

  private final String gameSpecificMessage;
  private final String eventName;
  private final int matchNumber;
  private final int replayNumber;
  private final MatchType matchType;
  private final Alliance alliance;
  private final int stationNumber;
  private final ControlWord fmsControlData;

  /**
   * Creates a new FMS info object.
   *
   * @param gameSpecificMessage the game-specific message sent by the FMS
   * @param eventName           the name of the event being played at
   * @param matchNumber         the match number being played
   * @param replayNumber        the number of the replay, or 0 if no replay is being played
   * @param matchType           the type of the match being played
   * @param alliance            the alliance the robot is on
   * @param stationNumber       the station number for this team
   * @param fmsControlData      miscellaneous FMS control data
   */
  public FmsInfo(String gameSpecificMessage,
                 String eventName,
                 int matchNumber,
                 int replayNumber,
                 MatchType matchType,
                 Alliance alliance,
                 int stationNumber,
                 ControlWord fmsControlData) {
    this.gameSpecificMessage = gameSpecificMessage;
    this.eventName = eventName;
    this.matchNumber = matchNumber;
    this.replayNumber = replayNumber;
    this.matchType = matchType;
    this.alliance = alliance;
    this.stationNumber = stationNumber;
    this.fmsControlData = fmsControlData;
  }

  /**
   * Creates a new FMS info object from a map.
   */
  public FmsInfo(Map<String, Object> map) {
    this(
        Maps.get(map, GAME_SPECIFIC_MESSAGE),
        Maps.get(map, EVENT_NAME),
        Maps.<String, Number>get(map, MATCH_NUMBER).intValue(),
        Maps.<String, Number>get(map, REPLAY_NUMBER).intValue(),
        MatchType.fromOrdinal(Maps.<String, Number>get(map, MATCH_TYPE).intValue()),
        Maps.get(map, IS_RED_ALLIANCE) ? Alliance.RED : Alliance.BLUE,
        Maps.<String, Number>get(map, STATION_NUMBER).intValue(),
        ControlWord.fromBits(Maps.<String, Number>get(map, FMS_CONTROL_DATA).intValue())
    );
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put(GAME_SPECIFIC_MESSAGE, gameSpecificMessage)
        .put(EVENT_NAME, eventName)
        .put(MATCH_NUMBER, matchNumber)
        .put(REPLAY_NUMBER, replayNumber)
        .put(MATCH_TYPE, matchType.ordinal())
        .put(IS_RED_ALLIANCE, alliance == Alliance.RED)
        .put(STATION_NUMBER, stationNumber)
        .put(FMS_CONTROL_DATA, fmsControlData.toBits())
        .build();
  }

  /**
   * Gets the game-specific message from the FMS.
   */
  public String getGameSpecificMessage() {
    return gameSpecificMessage;
  }

  /**
   * Gets the name of the event. This is the event code; for example, the Archimedes championship field will have an
   * event name of "ARCHIMEDES".
   */
  public String getEventName() {
    return eventName;
  }

  /**
   * Gets the number of the match being played.
   */
  public int getMatchNumber() {
    return matchNumber;
  }

  /**
   * Gets the replay number of the match. A value of zero means this is the first match between the two alliances;
   * a value of 1 means this is the first rematch and second match overall, and so on for increasing numbers.
   */
  public int getReplayNumber() {
    return replayNumber;
  }

  /**
   * Gets the type of the match being played.
   */
  public MatchType getMatchType() {
    return matchType;
  }

  /**
   * Gets the alliance color.
   */
  public Alliance getAlliance() {
    return alliance;
  }

  /**
   * Gets the station number.
   */
  public int getStationNumber() {
    return stationNumber;
  }

  /**
   * Gets the control information.
   */
  public ControlWord getFmsControlData() {
    return fmsControlData;
  }

}
