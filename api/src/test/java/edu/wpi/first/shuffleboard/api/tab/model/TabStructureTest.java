package edu.wpi.first.shuffleboard.api.tab.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TabStructureTest {

  private TabStructure structure;

  @BeforeEach
  public void setup() {
    structure = new TabStructure();
  }

  @Test
  public void testGetTab() {
    assertEquals(0, structure.getTabs().size(), "Structure should have no initial tabs");
    TabModel tab = structure.getTab("Title");
    assertAll(
        () -> assertEquals(1, structure.getTabs().size(), "Should be one tab now"),
        () -> assertEquals(tab, structure.getTabs().get("Title"))
    );
  }

  @Test
  public void testListeners() {
    AtomicInteger updates = new AtomicInteger(0);
    StructureChangeListener listener = s -> updates.incrementAndGet();
    structure.addStructureChangeListener(listener);

    structure.getTab("Tab 1");
    assertEquals(1, updates.get(), "Creating a new tab should have triggered the listener");

    structure.removeStructureChangeListener(listener);
    structure.getTab("Tab 2");
    assertEquals(1, updates.get(), "The listener was removed and should not have been fired");
  }

}
