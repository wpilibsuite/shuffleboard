package edu.wpi.first.shuffleboard.app.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Proxy;
import java.util.List;

public class PluginObjectInputStream extends ObjectInputStream {
  private final List<ClassLoader> classLoaders;

  /**
   * Constructs a new ClassLoaderObjectInputStream.
   *
   * @param classLoaders the list of ClassLoaders from which classes should be loaded
   * @param inputStream  the InputStream to work on
   * @throws IOException              if an I/O error occurs
   * @throws StreamCorruptedException if the stream is corrupted
   */
  public PluginObjectInputStream(
      List<ClassLoader> classLoaders, InputStream inputStream)
      throws IOException, StreamCorruptedException {
    super(inputStream);
    this.classLoaders = classLoaders;
  }

  /**
   * Resolve a class specified by the descriptor using the
   * specified ClassLoaders or the super ClassLoader.
   *
   * @param objectStreamClass descriptor of the class
   * @return the Class object described by the ObjectStreamClass
   * @throws IOException            if an I/O error occurs
   * @throws ClassNotFoundException if the Class cannot be found
   */
  @Override
  protected Class<?> resolveClass(ObjectStreamClass objectStreamClass)
      throws IOException, ClassNotFoundException {

    for (ClassLoader loader : classLoaders) {
      try {
        return Class.forName(objectStreamClass.getName(), false, loader);
      } catch (ClassNotFoundException ignored) {
        //This is ignored
      }
    }

    return super.resolveClass(objectStreamClass);
  }

  /**
   * Create a proxy class that implements the specified interfaces using
   * the specified ClassLoader or the super ClassLoader.
   *
   * @param interfaces the interfaces to implement
   * @return a proxy class implementing the interfaces
   * @throws IOException            if an I/O error occurs
   * @throws ClassNotFoundException if the Class cannot be found
   * @see java.io.ObjectInputStream#resolveProxyClass(java.lang.String[])
   * @since 2.1
   */
  @Override
  protected Class<?> resolveProxyClass(String[] interfaces) throws IOException,
      ClassNotFoundException {
    final Class<?>[] interfaceClasses = new Class[interfaces.length];
    for (int i = 0; i < interfaces.length; i++) {
      for (ClassLoader loader : classLoaders) {
        try {
          interfaceClasses[i] = Class.forName(interfaces[i], false, loader);
        } catch (ClassNotFoundException ignored) {
          //This is ignored
        }
      }
      if (interfaceClasses[i] == null) {
        return super.resolveProxyClass(interfaces);
      }
    }

    for (ClassLoader loader : classLoaders) {
      try {
        return Proxy.getProxyClass(loader, interfaceClasses);
      } catch (final IllegalArgumentException ignored) {
        //This is ignored
      }
    }

    return super.resolveProxyClass(interfaces);
  }

}