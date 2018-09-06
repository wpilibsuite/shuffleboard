package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.util.Storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The plugin cache is responsible for saving and loading external plugin jars so they can be automatically loaded or
 * "remembered" at startup.
 */
public class PluginCache {

  private static final Logger log = Logger.getLogger(PluginCache.class.getName());

  private static final Gson cacheGson = new Gson();
  private final Path cacheFile;

  private static volatile PluginCache defaultInstance;
  private static final PluginCache nullCache = new PluginCache(null);

  public PluginCache(Path cacheFile) {
    this.cacheFile = cacheFile;
  }

  /**
   * Gets the default plugin cache instance. If it does not exist, an attempt will be made to create it before returning
   * it. If this attempt fails, a "dummy" object will be returned instead whose save and load functions will not do
   * anything.
   */
  public static PluginCache getDefault() {
    if (defaultInstance == null) { // NOPMD not thread safe -- it absolutely is
      synchronized (PluginCache.class) {
        if (defaultInstance == null) {
          try {
            defaultInstance = new PluginCache(Storage.getPluginCache());
          } catch (IOException e) {
            log.log(Level.WARNING, "Could not create default plugin cache", e);
            return nullCache;
          }
        }
      }
    }
    return defaultInstance;
  }

  /**
   * Saves a list of URIs to plugin jars to the local cache.
   *
   * @param jarUris the URIs of the plugin jars to cache
   */
  public void saveToCache(List<URI> jarUris) {
    if (cacheFile == null) {
      log.warning("No cache file to save to");
      return;
    }
    String json = cacheGson.toJson(jarUris);
    try {
      log.finer(() -> "Caching plugin URIs " + jarUris);
      Files.write(cacheFile, json.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not save plugin cache", e);
    }
  }

  /**
   * Loads plugins from the cache.
   */
  public void loadCache(PluginLoader loader) {
    if (cacheFile == null) {
      log.warning("No cache file to load from");
      return;
    }
    try {
      String json = new String(Files.readAllBytes(cacheFile), StandardCharsets.UTF_8);
      List<URI> paths = new Gson().fromJson(json, new TypeToken<List<URI>>() {}.getType());
      if (paths != null) {

        // Load all plugin jars until no more can be loaded
        // This prevents issues caused when a plugin jar that appears earlier in the cache depends on plugins
        // that occur later in the cache.
        boolean anyPluginsLoadedOnLastAttempt;
        List<URI> successes = new ArrayList<>();
        do {
          anyPluginsLoadedOnLastAttempt = false;
          for (URI path : paths) {
            try {
              loader.loadPluginJar(path);
              successes.add(path);
              anyPluginsLoadedOnLastAttempt = true;
            } catch (IOException e) {
              log.log(Level.WARNING, "Could not read jar at cached path " + path, e);
            }
          }
          paths.removeAll(successes);
          successes.clear();
        } while (anyPluginsLoadedOnLastAttempt);
      }
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not read cache", e);
    }
  }

}
