package edu.wpi.first.shuffleboard.api.util;

/**
 * Utility class containing the values of commonly used system properties.
 */
public final class SystemProperties {

  /**
   * The name of the operating system.
   */
  public static final String OS_NAME = System.getProperty("os.name");

  /**
   * The version of the operating system.
   */
  public static final String OS_VERSION = System.getProperty("os.version");

  /**
   * The platform architecture of the operating system, eg amd64, x86, etc.
   */
  public static final String OS_ARCH = System.getProperty("os.arch");

  /**
   * The version of Java installed on the local machine.
   */
  public static final String JAVA_VERSION = System.getProperty("java.version");

  /**
   * The vendor of the Java installation.
   */
  public static final String JAVA_VENDOR = System.getProperty("java.vendor");

  /**
   * The name of the JRE that shuffleboard is running in.
   */
  public static final String JRE_NAME = System.getProperty("java.runtime.name");

  /**
   * The version of the JRE that shuffleboard is running in.
   */
  public static final String JRE_VERSION = System.getProperty("java.runtime.version");

  /**
   * The vendor of the local JVM.
   */
  public static final String JVM_VENDOR = System.getProperty("java.vm.vendor");

  /**
   * The version of the local JVM.
   */
  public static final String JVM_VERSION = System.getProperty("java.vm.version");

  /**
   * The specification version of the local JVM.
   */
  public static final String JVM_SPEC_VERSION = System.getProperty("java.vm.specification.version");

  /**
   * The system line separator character.
   */
  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * The filesystem directory separator character.
   */
  public static final String FILE_SEPARATOR = System.getProperty("file.separator");

  /**
   * The path to the user's home directory.
   */
  public static final String USER_HOME = System.getProperty("user.home");

  private SystemProperties() {
    throw new UnsupportedOperationException("This is a utility class");
  }

}
