package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.Alliance;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.ControlWord;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.FmsInfo;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.MatchType;

import java.util.Map;
import java.util.function.Function;

public final class FmsInfoType extends ComplexDataType<FmsInfo> {

  public static final FmsInfoType Instance = new FmsInfoType();

  private FmsInfoType() {
    super("FMSInfo", FmsInfo.class);
  }

  @Override
  public Function<Map<String, Object>, FmsInfo> fromMap() {
    return FmsInfo::new;
  }

  @Override
  public FmsInfo getDefaultValue() {
    return new FmsInfo("", "", 0, 0, MatchType.NONE, Alliance.RED, 0, ControlWord.fromBits(0));
  }
}
