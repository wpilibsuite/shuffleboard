package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.util.FxUtils;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;
import org.fxmisc.easybind.EasyBind;

import java.util.Timer;
import java.util.TimerTask;

/**
 * This is a class that is designed to be used as a Tab graphic,
 * it adds title editing and an onDragOver event.
 */
public class TabHandle extends StackPane {

  private static final int DRAG_FOCUS_DELAY = 500;
  private final Timer delayedDragThread = new Timer(true);
  private final HandledTab tab;

  private final Property<Node> tabHeaderContainer = new SimpleObjectProperty<>(this, "tabHeaderContainer");
  private final EventHandler<Event> fireFromTabContainer = event -> {
    if (!this.equals(event.getTarget())) {
      fireEvent(event);
    }
  };

  public TabHandle(HandledTab tab) {
    this.tab = tab;

    tabHeaderContainer.addListener((obs, oldContainer, newContainer) -> {
      if (newContainer != null) {
        newContainer.addEventHandler(EventType.ROOT, fireFromTabContainer);
      }
      if (oldContainer != null) {
        oldContainer.removeEventHandler(EventType.ROOT, fireFromTabContainer);
      }
    });
    tabHeaderContainer.bind(
            EasyBind.select(parentProperty()).select(Node::parentProperty).selectObject(Node::parentProperty)
    );

    if (tab.canEditTitle()) {
      EditableLabel label = new EditableLabel();
      label.textProperty().bindBidirectional(tab.titleProperty());
      getChildren().add(label);
    } else {
      Label label = new Label();
      label.textProperty().bind(tab.titleProperty());
      getChildren().add(label);
    }

    setOnDragEntered(this::startDelayedDrag);
  }

  private void startDelayedDrag(DragEvent dragEvent) {
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        FxUtils.runOnFxThread(tab::onDragOver);
      }
    };

    delayedDragThread.schedule(task, DRAG_FOCUS_DELAY);
    setOnDragExited(__de -> task.cancel());
  }
}
