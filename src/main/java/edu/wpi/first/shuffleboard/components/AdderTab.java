package edu.wpi.first.shuffleboard.components;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Tab;

public class AdderTab extends Tab {
  private Property<Runnable> onAddTab = new SimpleObjectProperty<>(this, "onAddTab", () -> {});

  AdderTab() {
    super("+");
    this.setOnSelectionChanged(__event -> {
      getOnAddTab().run();
      getTabPane().getSelectionModel().selectPrevious();
    });
  }

  public Runnable getOnAddTab() {
    return onAddTab.getValue();
  }

  public Property<Runnable> onAddTabProperty() {
    return onAddTab;
  }

  public void setOnAddTab(Runnable onAddTab) {
    this.onAddTab.setValue(onAddTab);
  }
}
