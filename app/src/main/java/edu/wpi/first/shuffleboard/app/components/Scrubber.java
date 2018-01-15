package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.util.FxUtils;

import java.util.Objects;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Slider;

/**
 * A special subclass of slider used for scrubbing a progress value. A scrubber has a default range of (0, 1) and will
 * only set the progress value when it is being controlled by a user; otherwise, it remains in a view-only mode to
 * prevent recursion.
 */
public class Scrubber extends Slider {

  private final BooleanProperty viewMode = new SimpleBooleanProperty(this, "viewMode", true);

  private Property<Number> progressProperty = null;
  private final ChangeListener<Number> progressListener = (__, oldProgress, newProgress) -> {
    if (isViewMode() && newProgress != null) {
      FxUtils.runOnFxThread(() -> setValue(newProgress.doubleValue()));
    }
  };

  /**
   * Creates a new scrubber.
   */
  public Scrubber() {
    setMin(0);
    setMax(1);
    setValue(0);
    valueProperty().addListener((__, oldPos, newPos) -> {
      if (!isViewMode() && oldPos.doubleValue() != newPos.doubleValue()) {
        progressProperty.setValue(newPos);
      }
    });
    setOnKeyPressed(e -> setViewMode(false));
    setOnKeyReleased(e -> setViewMode(true));
    setOnMousePressed(e -> setViewMode(false));
    setOnMouseReleased(e -> setViewMode(true));
  }

  /**
   * Sets the progress property to scrub.
   */
  public void setProgressProperty(Property<Number> progressProperty) {
    Objects.requireNonNull(progressProperty, "progressProperty");
    if (this.progressProperty != null) {
      this.progressProperty.removeListener(progressListener);
    }
    this.progressProperty = progressProperty;
    progressProperty.addListener(progressListener);
  }

  public final boolean isViewMode() {
    return viewMode.get();
  }

  public final ReadOnlyBooleanProperty viewModeProperty() {
    return viewMode;
  }

  private void setViewMode(boolean viewMode) {
    this.viewMode.set(viewMode);
  }

}
