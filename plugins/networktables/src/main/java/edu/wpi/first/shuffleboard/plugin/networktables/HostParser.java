package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.util.LazyInit;

import edu.wpi.first.networktables.NetworkTableInstance;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses a raw host string input to a consistent format.
 */
public final class HostParser {

  private static final Logger log = Logger.getLogger(HostParser.class.getName());

  public static final int DEFAULT_PORT = NetworkTableInstance.kDefaultPort;

  /**
   * Information about a NetworkTable server host.
   */
  static final class NtHostInfo {
    private final String host;
    private final int port;

    private final LazyInit<OptionalInt> team = LazyInit.of(() -> {
      if (getHost().matches("\\d{1,4}")) {
        return OptionalInt.of(Integer.parseInt(getHost()));
      } else {
        return OptionalInt.empty();
      }
    });

    public static NtHostInfo onDefaultPort(String host) {
      return new NtHostInfo(host, DEFAULT_PORT);
    }

    public NtHostInfo(String host, int port) {
      this.host = Objects.requireNonNull(host, "host");
      this.port = port;
    }

    /**
     * Gets the host of the NetworkTable server. This can be an IP address like {@code "10.TE.AM.2"}, an mDNS address
     * like {@code "roborio-TEAM-frc.local"}, or a team number like {@code "190"}.
     */
    public String getHost() {
      return host;
    }

    /**
     * Gets the team number for the roborio host, if specified.
     *
     * @return an optional integer for the team number, if one was specified
     */
    public OptionalInt getTeam() {
      return team.get();
    }

    /**
     * Gets the port the server is running on.
     */
    public int getPort() {
      return port;
    }
  }

  /**
   * Parses the given input host string to a consistent format.
   *
   * @param rawHost the raw host like "190", "roborio-190-frc.local", "190:1736", etc.
   *
   * @return the host info for the given raw host information, or an empty optional if the input string is invalid
   */
  public Optional<NtHostInfo> parse(String rawHost) {
    try {
      // Make sure the URI starts with http:// or https// so the URI constructor can parse it
      String str = rawHost.startsWith("http://") || rawHost.startsWith("https://") ? rawHost : "http://" + rawHost;

      URI uri = new URI(str);
      String host = uri.getHost();

      if (host == null) {
        log.warning("No host or invalid host specified: '" + rawHost + "'");
        return Optional.empty();
      }

      int port = uri.getPort();

      if (port == -1) {
        return Optional.of(NtHostInfo.onDefaultPort(host));
      } else {
        return Optional.of(new NtHostInfo(host, port));
      }
    } catch (URISyntaxException e) {
      log.log(Level.WARNING, "Invalid NetworkTables host: " + rawHost, e);
      return Optional.empty();
    }
  }

}
