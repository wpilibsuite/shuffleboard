package edu.wpi.first.shuffleboard.api.sources;

import java.util.Objects;

/**
 * Contains information about the state of a source type's connection to the source of its data.
 */
public final class ConnectionStatus {

  public static final ConnectionStatus Nil = new ConnectionStatus("Unknown", false);

  private final String host;
  private final boolean connected;

  public ConnectionStatus(String host, boolean connected) {
    this.host = Objects.requireNonNull(host, "host");
    this.connected = connected;
  }

  /**
   * Gets the host(s) that the source type is connected to.
   */
  public String getHost() {
    return host;
  }

  /**
   * Checks if the source type connected to the {@link #getHost() host(s)}.
   */
  public boolean isConnected() {
    return connected;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ConnectionStatus that = (ConnectionStatus) obj;
    return this.connected == that.connected
        && this.host.equals(that.host);
  }

  @Override
  public int hashCode() {
    return Objects.hash(connected, host);
  }

  @Override
  public String toString() {
    return String.format("ConnectionStatus(host='%s', connected=%s)", host, connected);
  }

}
