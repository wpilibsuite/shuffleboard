package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.ConnectionStatus;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.UiHints;

import org.fxmisc.easybind.EasyBind;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import static edu.wpi.first.shuffleboard.api.util.ListUtils.joining;

/**
 * Controller for the connection indicators in the bottom-right corner of the application window.
 */
public final class ConnectionIndicatorsController {

  @FXML
  private Pane root;

  private final ListChangeListener<SourceType> updateWhenSourcesChange =
      change -> generateConnectionIndicators(change.getList());

  @FXML
  private void initialize() {
    generateConnectionIndicators(SourceTypes.getDefault().getItems());
    SourceTypes.getDefault().getItems().addListener(updateWhenSourcesChange);
  }

  private void generateConnectionIndicators(List<? extends SourceType> sourceTypes) {
    root.getChildren().setAll(
        sourceTypes.stream()
            .filter(s -> !optOutOfConnectionIndicator(s))
            .map(this::generateConnectionLabel)
            .collect(joining(this::generateSeparatorLabel)));
  }

  private Label generateSeparatorLabel() {
    Label label = new Label(" | ");
    label.getStyleClass().add("connection-indicator-separator");
    return label;
  }

  /**
   * Checks if a source type has opted out of displaying a connection indicator.
   */
  private boolean optOutOfConnectionIndicator(SourceType sourceType) {
    Class<? extends SourceType> clazz = sourceType.getClass();
    UiHints hints = clazz.getAnnotation(UiHints.class);
    return hints != null && !hints.showConnectionIndicator();
  }

  private Label generateConnectionLabel(SourceType sourceType) {
    Label label = new Label();
    label.getStyleClass().add("connection-indicator");
    label.textProperty().bind(
        EasyBind.monadic(sourceType.connectionStatusProperty())
            .map(ConnectionStatus::isConnected)
            .map(connected -> sourceType.getName() + ": " + (connected ? "connected" : "not connected")));
    sourceType.connectionStatusProperty().addListener((__, old, status) -> {
      updateConnectionLabel(label, status.isConnected());
    });
    updateConnectionLabel(label, sourceType.getConnectionStatus().isConnected());
    return label;
  }

  private void updateConnectionLabel(Label label, boolean connected) {
    label.pseudoClassStateChanged(PseudoClass.getPseudoClass("connected"), connected);
    label.pseudoClassStateChanged(PseudoClass.getPseudoClass("disconnected"), !connected);
  }

}
