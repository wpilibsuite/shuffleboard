package edu.wpi.first.shuffleboard.api.plugin;

/**
 * An exception that can be thrown when a plugin has an invalid definition, e.g. the version string doesn't follow
 * semantic versioning.
 */
public class InvalidPluginDefinitionException extends RuntimeException {

  public InvalidPluginDefinitionException(String message) {
    super(message);
  }

  public InvalidPluginDefinitionException(String message, Throwable cause) {
    super(message, cause);
  }

}
