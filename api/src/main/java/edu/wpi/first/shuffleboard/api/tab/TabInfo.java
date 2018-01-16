package edu.wpi.first.shuffleboard.api.tab;

import java.util.Objects;

/**
 * Represents a tab in the dashboard.
 */
public final class TabInfo {

  private final String name;
  private final boolean autoPopulate;
  private final String sourcePrefix;

  /**
   * Creates a new TabInfo object.
   *
   * @param name         the name of the tab
   * @param autoPopulate whether or not the tab should autopopulate
   * @param sourcePrefix the source prefix to use when autopopulating
   */
  public TabInfo(String name, boolean autoPopulate, String sourcePrefix) {
    this.name = Objects.requireNonNull(name, "name");
    this.autoPopulate = autoPopulate;
    this.sourcePrefix = Objects.requireNonNull(sourcePrefix, "sourcePrefix");
  }

  /**
   * Gets the name of the tab this represents.
   */
  public String getName() {
    return name;
  }

  /**
   * Checks if the tab should autopopulate.
   */
  public boolean isAutoPopulate() {
    return autoPopulate;
  }

  /**
   * The source prefix the tab should use when autopopulating.
   */
  public String getSourcePrefix() {
    return sourcePrefix;
  }

  public static TabInfoBuilder builder() {
    return new TabInfoBuilder();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TabInfo tabInfo = (TabInfo) o;
    return autoPopulate == tabInfo.autoPopulate
        && Objects.equals(name, tabInfo.name)
        && Objects.equals(sourcePrefix, tabInfo.sourcePrefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, autoPopulate, sourcePrefix);
  }

  @Override
  public String toString() {
    return String.format("TabInfo(name='%s', autoPopulate=%s, sourcePrefix='%s')", name, autoPopulate, sourcePrefix);
  }

  public static final class TabInfoBuilder {

    private String name;
    private boolean autoPopulate = false;
    private String sourcePrefix = "";

    /**
     * Sets the name of the built tab.
     */
    public TabInfoBuilder name(String name) {
      this.name = Objects.requireNonNull(name, "name");
      return this;
    }

    /**
     * Sets the built tab to autopopulate. Autopopulation is {@code false} by default; use this method to enable it.
     */
    public TabInfoBuilder autoPopulate() {
      this.autoPopulate = true;
      return this;
    }

    /**
     * Sets the source prefix the built tab should use during autopopulation.
     */
    public TabInfoBuilder sourcePrefix(String sourcePrefix) {
      this.sourcePrefix = Objects.requireNonNull(sourcePrefix, "sourcePrefix");
      return this;
    }

    /**
     * Creates a new TabInfo object from this builder.
     *
     * @throws IllegalStateException if the tab name was not set with {@link #name}
     * @throws IllegalStateException if the tab was set to autopopulate but no source prefix was provided
     */
    public TabInfo build() {
      if (name == null) {
        throw new IllegalStateException("Tab name was not set");
      }
      if (autoPopulate && sourcePrefix.isEmpty()) {
        throw new IllegalStateException("A tab cannot autopopulate with no source prefix set");
      }
      return new TabInfo(name, autoPopulate, sourcePrefix);
    }

  }


}
