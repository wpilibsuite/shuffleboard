package edu.wpi.first.shuffleboard.testplugins;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

/**
 * A basic plugin with no dependencies.
 */
public class BasicPlugin extends Plugin {

  public BasicPlugin() {
    super("edu.wpi.first.shuffleboard", "Basic Plugin", "1.0.0", "A basic plugin for testing");
  }

}
