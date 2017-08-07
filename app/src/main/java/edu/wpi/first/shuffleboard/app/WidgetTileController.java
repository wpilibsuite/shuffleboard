package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.WidgetTile;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.widget.WidgetPropertySheet;

import org.fxmisc.easybind.EasyBind;

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
    titleLabel.textProperty().bind(
        EasyBind.monadic(tile.widgetProperty())
            .selectProperty(Widget::sourceProperty)
            .selectProperty(DataSource::nameProperty));
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
    WidgetPropertySheet propertySheet = new WidgetPropertySheet(widget);
    propertySheet.setOnDragDetected(tile.getOnDragDetected());
    propertySheet.addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);
    return propertySheet;
  }

}
