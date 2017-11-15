package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Dependency;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;

/**
 * A plugin that depends on another plugin.
 */
@Description(group = "edu.wpi.first.shuffleboard", name = "DependentPlugin", version = "1.0.0", summary = "")
@Dependency(group = "edu.wpi.first.shuffleboard", name = "BasicPlugin", minVersion = "1.0.0")
public final class DependentPlugin extends Plugin {
}
