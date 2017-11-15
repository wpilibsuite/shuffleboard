package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Dependency;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;

/**
 * A plugin that has a dependency on another, unknown, plugin.
 */
@Description(group = "edu.wpi.first.shuffleboard", name = "DependentOnUnknownPlugin", version = "1.0.0", summary = "")
@Dependency(group = "???", name = "???", minVersion = "0.0.0")
public final class DependentOnUnknownPlugin extends Plugin {

}
