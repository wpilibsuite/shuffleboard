package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

/**
 * A plugin that has a dependency on another, unknown, plugin.
 */
public class DependentOnUnknownPlugin extends Plugin {

  public DependentOnUnknownPlugin() {
    super("edu.wpi.first.shuffleboard", "DependentOnUnknownPlugin", "1.0.0", "");
    addDependency("???????", "???????", "0.0.0");
  }

}
