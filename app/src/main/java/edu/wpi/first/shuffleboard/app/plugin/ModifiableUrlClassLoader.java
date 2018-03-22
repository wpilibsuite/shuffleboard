package edu.wpi.first.shuffleboard.app.plugin;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * A type of {@code URLClassLoader} that can have URLs added to it after instantiation.
 */
public class ModifiableUrlClassLoader extends URLClassLoader {

  private static final URL[] EMPTY_URLS = new URL[0];

  /**
   * Constructs a new modifiable URLClassLoader for the given URLs. The URLs will be
   * searched in the order specified for classes and resources after first
   * searching in the specified parent class loader. Any URL that ends with
   * a '/' is assumed to refer to a directory. Otherwise, the URL is assumed
   * to refer to a JAR file which will be downloaded and opened as needed.
   *
   * @param urls   the URLs from which to load classes and resources
   * @param parent the parent class loader for delegation
   *
   * @throws NullPointerException if {@code urls} is {@code null}.
   */
  public ModifiableUrlClassLoader(URL[] urls, ClassLoader parent) {
    super(urls, parent);
  }

  /**
   * Constructs a new modifiable URLClassLoader with no initial URLs. URLs can be added
   * later with {@link #addURL(URL)}.
   *
   * @param parent the parent class loader for delegation
   */
  public ModifiableUrlClassLoader(ClassLoader parent) {
    this(EMPTY_URLS, parent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("PMD.UselessOverridingMethod") // This PMD rule is bugged, so we have to suppress it
  public void addURL(URL url) {
    super.addURL(url);
  }

}
