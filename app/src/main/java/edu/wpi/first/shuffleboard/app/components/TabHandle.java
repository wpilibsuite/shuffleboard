package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
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

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This is a class that is designed to be used as a Tab graphic,
 * it adds title editing and an onDragOver event.
 */
public class TabHandle extends StackPane {

  private static final int DRAG_FOCUS_DELAY = 500;
  private final ScheduledExecutorService delayedDragService = ThreadUtils.newDaemonScheduledExecutorService();
  private final HandledTab tab;

  private final Property<Node> tabHeaderContainer = new SimpleObjectProperty<>(this, "tabHeaderContainer");
  private final EventHandler<Event> fireFromTabContainer = event -> {
    if (!this.equals(event.getTarget())) {
      fireEvent(event);
    }
  };

  /**
   * Creates a TabHandle for a specific Tab. This TabHandle should be retained by the tab,
   * or else specific event handlers might be garbage collected.
   */
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

  @SuppressWarnings("PMD.UnusedFormalParameter")
  private void startDelayedDrag(DragEvent dragEvent) {
    Future<?> task = delayedDragService.schedule(
        () -> FxUtils.runOnFxThread(tab::onDragOver),
        DRAG_FOCUS_DELAY, TimeUnit.MILLISECONDS);

    setOnDragExited(__ -> task.cancel(false));
  }
}
