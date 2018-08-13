package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

public abstract class DriveBaseData<T extends DriveBaseData<T>> extends ComplexData<T> {

  private final boolean controllable;

  protected DriveBaseData(boolean controllable) {
    this.controllable = controllable;
  }

  public final boolean isControllable() {
    return controllable;
  }
}
