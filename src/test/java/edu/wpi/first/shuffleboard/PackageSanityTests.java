package edu.wpi.first.shuffleboard;

import com.google.common.testing.AbstractPackageSanityTests;

public class PackageSanityTests extends AbstractPackageSanityTests {

  public PackageSanityTests() {
    ignoreClasses(ShuffleBoard.class::equals);
    ignoreClasses(NetworkTableEntry.class::equals);
    ignoreClasses(MainWindowController.class::equals);
  }

}
