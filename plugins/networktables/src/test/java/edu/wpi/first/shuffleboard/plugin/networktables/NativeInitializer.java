package edu.wpi.first.shuffleboard.plugin.networktables;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.wpiutil.CombinedRuntimeLoader;
import edu.wpi.first.wpiutil.WPIUtilJNI;

public class NativeInitializer implements BeforeAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);

    CombinedRuntimeLoader.loadLibraries(NativeInitializer.class, "ntcorejni");
  }

}
