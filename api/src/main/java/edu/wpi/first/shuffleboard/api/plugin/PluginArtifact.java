package edu.wpi.first.shuffleboard.api.plugin;

import com.cedarsoft.version.Version;

import java.util.Objects;

/**
 * Represents a plugin in the form of a maven-style artifact with a group ID, a name, and a version. Group IDs should
 * be unique to avoid conflicts with other plugins with the same name but from different developers. We recommend using
 * the reverse-DNS naming scheme that Java recommends for package names. Versions strings should follow the
 * <a href="http://www.semver.org">Semantic Versioning</a> format.
 */
public final class PluginArtifact {

  private final String groupId;
  private final String name;
  private final String idString;
  private final Version version;

  /**
   * Creates a new plugin artifact.
   *
   * @param groupId the ID of the group that develops the plugin
   * @param name    the name of the plugin
   * @param version the version of the plugin
   *
   * @throws IllegalArgumentException if the version does not follow semantic versioning
   */
  public PluginArtifact(String groupId, String name, String version) {
    this.groupId = groupId;
    this.name = name;
    this.idString = groupId + ":" + name;
    this.version = Version.parse(version);
  }

  /**
   * Gets the group ID of this artifact.
   */
  public String getGroupId() {
    return groupId;
  }

  public String getName() {
    return name;
  }

  public String getIdString() {
    return idString;
  }

  public Version getVersion() {
    return version;
  }

  /**
   * Creates a gradle-style string representing this artifact. The string is in the format
   * {@code "{groupId}:{name}:{version}"}
   */
  public String toGradleString() {
    return groupId + ":" + name + ":" + version;
  }

  @Override
  public String toString() {
    return String.format("PluginArtifact(groupId=%s, name=%s, version=%s)", groupId, name, version);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof PluginArtifact)) {
      return false;
    }
    PluginArtifact that = (PluginArtifact) obj;
    return this.groupId.equals(that.groupId)
        && this.name.equals(that.name)
        && this.version.equals(that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupId, name, version);
  }

}
