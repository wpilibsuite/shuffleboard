package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.PropertyBinding;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends Tile<Widget> {

  /**
   * Pseudoclass used on tiles when the widget inside loses its source.
   */
  private static final PseudoClass NO_SOURCE = PseudoClass.getPseudoClass("no-source");

  private final BooleanProperty showWidget = new SimpleBooleanProperty(this, "showWidget", true);

  // Store as a field to prevent GC
  private PropertyBinding<DataSource> retainedSource; //NOPMD could be a local variable

  /**
   * Creates a tile with the given widget and size.
   */
  public WidgetTile(Widget widget, TileSize size) {
    this();
    setContent(widget);
    setSize(size);
  }

  private WidgetTile() {
    super();
    addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);

    retainedSource = EasyBind.monadic(contentProperty()).selectProperty(Widget::sourceProperty);
    retainedSource.addListener((__, oldSource, newSource) -> {
      if (newSource instanceof DestroyedSource) {
        pseudoClassStateChanged(NO_SOURCE, true);
        setDisable(true);
      } else {
        pseudoClassStateChanged(NO_SOURCE, false);
        setDisable(false);
      }
    });

    titleProperty().bind(
            EasyBind.monadic(contentProperty())
                    .selectProperty(Widget::sourceProperty)
                    .selectProperty(DataSource::nameProperty));

    centerProperty().unbind();
    centerProperty().bind(Bindings.createObjectBinding(
            this::createCenter, contentProperty(), showWidgetProperty()));
  }

  private void changeView(MouseEvent event) {
    if (event.getClickCount() == 2) {
      toggleShowWidget();
    }
  }

  private Node createCenter() {
    if (getContent() == null) {
      return null;
    } else if (isShowWidget() || getContent().getProperties().isEmpty()) {
      return getContent().getView();
    } else {
      return createPrefsController(getContent());
    }
  }

  private Node createPrefsController(Widget widget) {
    WidgetPropertySheet propertySheet = new WidgetPropertySheet(widget.getProperties());
    propertySheet.setOnDragDetected(getOnDragDetected());
    propertySheet.addEventHandler(MouseEvent.MOUSE_CLICKED, this::changeView);
    return propertySheet;
  }

  public boolean isShowWidget() {
    return showWidget.get();
  }

  public BooleanProperty showWidgetProperty() {
    return showWidget;
  }

  public void setShowWidget(boolean showWidget) {
    this.showWidget.set(showWidget);
  }

  public void toggleShowWidget() {
    setShowWidget(!isShowWidget());
  }

}
