package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.util.OsDetector;
import edu.wpi.first.shuffleboard.api.util.SystemProperties;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.StringJoiner;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Pane;

/**
 * Controller for the "About" dialog.
 */
@ParametrizedController("AboutDialogPane.fxml")
public final class AboutDialogController {

  @FXML
  private Pane root;
  @FXML
  private Label versionAndBuildInfo;
  @FXML
  private Label jreInfo;
  @FXML
  private Label osInfo;

  @FXML
  private void initialize() {
    versionAndBuildInfo.setText(createBuildInfoText());
    jreInfo.setText(createJreInfoText());
    osInfo.setText(createOsText());
  }

  private String createBuildInfoText() {
    return "Version " + Shuffleboard.getVersion() + " built on " + humanReadable(Shuffleboard.getBuildTime());
  }

  /**
   * Converts an instance to a human readable date in the format {@code "<month> <day of month>, <year>"}, for example
   * "January 1, 2018" or "April 22, 2017".
   */
  private String humanReadable(Instant time) {
    LocalDateTime local = LocalDateTime.ofInstant(time, ZoneId.of("EST", ZoneId.SHORT_IDS));
    return Month.from(local).getDisplayName(TextStyle.FULL, Locale.getDefault())
        + " " + local.getDayOfMonth()
        + ", " + local.getYear();
  }

  private String createOsText() {
    String name = SystemProperties.OS_NAME;
    String version = SystemProperties.OS_VERSION;
    String arch = SystemProperties.OS_ARCH;
    if (OsDetector.isLinux()) {
      return String.format("Operating system: %s %s %s (%s)", name, version, arch, OsDetector.getLinuxDistribution());
    } else {
      return String.format("Operating system: %s %s %s", name, version, arch);
    }
  }

  private String createJreInfoText() {
    return "JRE: " + SystemProperties.JRE_VERSION + " -- " + SystemProperties.JAVA_VENDOR;
  }

  /**
   * Copies all the technical info to the system clipboard.
   */
  @FXML
  private void copyTechInfoToClipboard() {
    StringJoiner joiner = new StringJoiner(SystemProperties.LINE_SEPARATOR)
        .add(createBuildInfoText())
        .add(createJreInfoText())
        .add(createOsText());
    ClipboardContent content = new ClipboardContent();
    content.putString(joiner.toString());
    Clipboard.getSystemClipboard().setContent(content);
    root.requestFocus();
  }

}
