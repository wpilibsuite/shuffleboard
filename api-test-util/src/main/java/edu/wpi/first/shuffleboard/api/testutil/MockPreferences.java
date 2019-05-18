package edu.wpi.first.shuffleboard.api.testutil;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

/**
 * A mock preferences class that stores values in a {@link Map Map&lt;String, String&gt;}.
 * This cannot have child preference nodes.
 */
public class MockPreferences extends AbstractPreferences {

  private final Map<String, String> map = new HashMap<>();

  public MockPreferences() {
    super(null, "");
  }

  @Override
  protected void putSpi(String key, String value) {
    map.put(key, value);
  }

  @Override
  protected String getSpi(String key) {
    return map.get(key);
  }

  @Override
  protected void removeSpi(String key) {
    map.remove(key);
  }

  @Override
  protected void removeNodeSpi() throws BackingStoreException {
    // no child nodes
  }

  @Override
  protected String[] keysSpi() throws BackingStoreException {
    return map.keySet().toArray(new String[map.keySet().size()]);
  }

  @Override
  protected String[] childrenNamesSpi() throws BackingStoreException {
    return new String[0]; // no children
  }

  @Override
  protected AbstractPreferences childSpi(String name) {
    return null;
  }

  @Override
  protected void syncSpi() throws BackingStoreException {
    // nothing to sync with
  }

  @Override
  protected void flushSpi() throws BackingStoreException {
    // nothing to flush to
  }
}
