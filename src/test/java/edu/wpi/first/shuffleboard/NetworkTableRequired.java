package edu.wpi.first.shuffleboard;

import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

/**
 * A utility class for creating network table instances. This should be used in set up and tear down
 * methods in test classes for making sure each test has its own fresh network table environment.
 */
public final class NetworkTableRequired {

  private NetworkTableRequired() {
  }

  /**
   * Sets up the network table server on the default port.
   */
  public static void setUpNetworkTables() {
    setUpNetworkTables(12345);
  }

  /**
   * Sets up the network table server, using the given port.
   */
  public static void setUpNetworkTables(int serverPort) {
    NetworkTablesJNI.stopClient();
    NetworkTablesJNI.stopServer();
    NetworkTablesJNI.setServer("test_server", serverPort);
  }

  /**
   * Shuts down the testing server.
   */
  public static void tearDownNetworkTables() {
    NetworkTablesJNI.stopServer();
  }

}
