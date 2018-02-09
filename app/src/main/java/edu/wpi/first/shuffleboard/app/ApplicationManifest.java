package edu.wpi.first.shuffleboard.app;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A helper class for extracting information from the manifest of the application JAR. This class is only useful when
 * running shuffleboard from a JAR; running from source (eg with gradle) will not generate a manifest file.
 */
public final class ApplicationManifest {

  private static final Logger log = Logger.getLogger(ApplicationManifest.class.getName());

  private static final Manifest manifest;

  /**
   * The attribute name for the implementation version in the manifest.
   */
  public static final Attributes.Name IMPLEMENTATION_VERSION = new Attributes.Name("Implementation-Version");

  /**
   * The attribute name for the application built date in the manifest.
   */
  public static final Attributes.Name BUILT_DATE = new Attributes.Name("Built-Date");

  private static final Instant buildTime;

  private ApplicationManifest() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  static {
    manifest = readManifest().orElse(new Manifest());
    buildTime = Instant.parse((String) manifest.getMainAttributes().getOrDefault(BUILT_DATE, Instant.now().toString()));
  }

  private static Optional<Manifest> readManifest() {
    try {
      return Collections.list(Shuffleboard.class.getClassLoader().getResources("META-INF/MANIFEST.MF"))
          .stream()
          .filter(u -> u.toString().contains(Shuffleboard.getRunningLocation()))
          .flatMap(u -> unsafeGet(u::openStream))
          .flatMap(in -> unsafeGet(() -> new Manifest(in)))
          .findFirst();
    } catch (IOException e) {
      log.log(Level.WARNING, "The manifest file could not be read", e);
      return Optional.empty();
    }
  }

  private static <T> Stream<T> unsafeGet(Callable<T> callable) {
    try {
      return Stream.of(callable.call());
    } catch (Exception e) {
      return Stream.empty();
    }
  }

  /**
   * Gets the manifest file. If shuffleboard is not running from a JAR, this will return an empty manifest with no
   * attributes. Users should be careful to use default values when getting attributes from the manifest.
   */
  public static Manifest getManifest() {
    return manifest;
  }

  /**
   * Gets the instant at which the application JAR was built. If shuffleboard is not running from a JAR, this will
   * return the instant at which this class was loaded.
   */
  public static Instant getBuildTime() {
    return buildTime;
  }

}
