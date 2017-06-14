package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.util.FxUtils;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.layout.StackPane;

import java.util.Timer;
import java.util.TimerTask;

public class TabHandle extends StackPane {

  private static final int DRAG_FOCUS_DELAY = 500;
  private final Timer delayedDragThread = new Timer(true);
  private final HandledTab tab;

  /**
   * This is a class that is designed to be used as a Tab graphic,
   * it adds title editing and focus-on-drag.
   */
  public TabHandle(HandledTab tab) {
    this.tab = tab;

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
        FxUtils.runOnFxThread(tab::focus);
      }
    };

    delayedDragThread.schedule(task, DRAG_FOCUS_DELAY);
    setOnDragExited(__de -> task.cancel());
  }
}
