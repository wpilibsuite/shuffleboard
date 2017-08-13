package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.components.WidgetPropertySheet;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.PropertyBinding;

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

  // Store as a field to prevent GC
  private PropertyBinding<DataSource> sourceProperty;

  /**
   * Pseudoclass used on tiles when the widget inside loses its source.
   */
  private static final PseudoClass NO_SOURCE = PseudoClass.getPseudoClass("no-source");

  @FXML
  private void initialize() {
    tile.addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);
    sourceProperty = EasyBind.monadic(tile.widgetProperty()).selectProperty(Widget::sourceProperty);
    titleLabel.textProperty().bind(sourceProperty.selectProperty(DataSource::nameProperty));
    tile.centerProperty().bind(
        Bindings.createObjectBinding(
            this::createCenter, tile.widgetProperty(), tile.showWidgetProperty()));
    sourceProperty.addListener((__, oldSource, newSource) -> {
      if (newSource instanceof DestroyedSource) {
        tile.pseudoClassStateChanged(NO_SOURCE, true);
        tile.setDisable(true);
      } else {
        tile.pseudoClassStateChanged(NO_SOURCE, false);
        tile.setDisable(false);
      }
    });
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
