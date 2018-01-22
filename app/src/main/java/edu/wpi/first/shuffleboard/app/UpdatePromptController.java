package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.FxUtils;

import com.github.zafarkhaja.semver.Version;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

/**
 * Controller for the update prompt.
 */
public final class UpdatePromptController {

  @FXML
  private Pane root;
  @FXML
  private Label infoLabel;

  private static final Version nullVersion = Version.forIntegers(0, 0, 0);
  private final Property<Version> currentVersion = new SimpleObjectProperty<>(this, "currentVersion", nullVersion);
  private final Property<Version> newestVersion = new SimpleObjectProperty<>(this, "mostRecentVersion", nullVersion);

  @FXML
  private void initialize() {
    FxUtils.setController(root, this);
    MonadicBinding<String> text = EasyBind.combine(currentVersion, newestVersion, (current, newest) -> {
      StringBuilder base = new StringBuilder()
          .append("The current version of shuffleboard is ").append(current)
          .append("\nThe newest version is ").append(newest)
          .append("\nDo you want to download and install this update?");
      if (newest.getMajorVersion() > current.getMajorVersion()) {
        base.append("\n\nWARNING: There has been a major version bump! Custom plugins and widgets may no longer work.");
      }
      return base.toString();
    });
    infoLabel.textProperty().bind(text);
  }

  public Version getCurrentVersion() {
    return currentVersion.getValue();
  }

  public Property<Version> currentVersionProperty() {
    return currentVersion;
  }

  public void setCurrentVersion(Version currentVersion) {
    this.currentVersion.setValue(currentVersion);
  }

  public Version getNewestVersion() {
    return newestVersion.getValue();
  }

  public Property<Version> newestVersionProperty() {
    return newestVersion;
  }

  public void setNewestVersion(Version newestVersion) {
    this.newestVersion.setValue(newestVersion);
  }
}
