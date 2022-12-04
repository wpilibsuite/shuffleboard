package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import edu.wpi.first.cscore.CameraServerJNI;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.util.CombinedRuntimeLoader;
import edu.wpi.first.util.WPIUtilJNI;

public class NativeInitializer implements BeforeAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    WPIUtilJNI.Helper.setExtractOnStaticLoad(false);
    NetworkTablesJNI.Helper.setExtractOnStaticLoad(false);
    CameraServerJNI.Helper.setExtractOnStaticLoad(false);

    CombinedRuntimeLoader.loadLibraries(NativeInitializer.class, "ntcorejni", "wpiutiljni", "cscorejnicvstatic");
  }

}
