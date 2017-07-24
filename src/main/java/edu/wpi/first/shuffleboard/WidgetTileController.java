package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.components.WidgetTile;
import edu.wpi.first.shuffleboard.util.PropertyUtils;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.WidgetPropertySheet;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

public class WidgetTileController {

  @FXML
  private WidgetTile tile;
  @FXML
  private Label titleLabel;

  @FXML
  private void initialize() {
    tile.addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);
    PropertyUtils.bindWithConverter(
        titleLabel.textProperty(),
        tile.widgetProperty(),
        w -> w.getSource().getName());
    tile.centerProperty().bind(
        Bindings.createObjectBinding(
            this::createCenter, tile.widgetProperty(), tile.showWidgetProperty()));
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
