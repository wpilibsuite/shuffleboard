package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.components.WidgetPropertySheet;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class WidgetTileController {

  @FXML
  private WidgetTile tile;
  @FXML
  private Label titleLabel;

  /**
   * Pseudoclass used on tiles when the widget inside loses its source.
   */
  private static final PseudoClass NO_SOURCE = PseudoClass.getPseudoClass("no-source");

  @FXML
  private void initialize() {
    tile.addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);
    tile.widgetProperty().addListener((__, oldWidget, widget) -> {
      setTitleFor(widget);
      widget.getSources().addListener((InvalidationListener) o -> setTitleFor(widget));
      widget.getSources().addListener((InvalidationListener) o -> {
        boolean noSources = widget.getSources().stream().allMatch(s -> s instanceof DestroyedSource);
        tile.pseudoClassStateChanged(NO_SOURCE, noSources);
        tile.setDisable(noSources);
      });
    });
    setTitleFor(tile.getWidget());
    tile.centerProperty().bind(
        Bindings.createObjectBinding(
            this::createCenter, tile.widgetProperty(), tile.showWidgetProperty()));
  }

  private void setTitleFor(Widget widget) {
    if (widget == null) {
      titleLabel.setText("");
      return;
    }
    if (widget.getSources().size() == 1) {
      titleLabel.setText(widget.getSources().get(0).getName());
    } else {
      titleLabel.setText(String.format("%s (%d Sources)", widget.getName(), widget.getSources().size()));
    }
  }

  private void changeView(MouseEvent event) {
    if (event.getClickCount() == 2) {
      tile.toggleShowWidget();
    }
  }

  private Node createCenter() {
    if (tile.getWidget() == null) {
      return null;
    } else if (tile.isShowWidget() || tile.getWidget().getProperties().isEmpty()) {
      return tile.getWidget().getView();
    } else {
      return createPrefsController(tile.getWidget());
    }
  }

  private Node createPrefsController(Widget widget) {
    WidgetPropertySheet propertySheet = new WidgetPropertySheet(widget.getProperties());
    propertySheet.setOnDragDetected(tile.getOnDragDetected());
    propertySheet.addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);
    return propertySheet;
  }

}
