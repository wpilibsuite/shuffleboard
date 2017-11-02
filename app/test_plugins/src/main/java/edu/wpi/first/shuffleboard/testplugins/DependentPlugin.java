package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

/**
 * A plugin that depends on another plugin.
 */
public class DependentPlugin extends Plugin {

  public DependentPlugin() {
    super("edu.wpi.first.shuffleboard", "Dependent Plugin", "1.0.0", "A plugin that depends on another plugin");
    addDependency("edu.wpi.first.shuffleboard", "Basic Plugin", "1.0.0");
  }

}
