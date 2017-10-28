package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;
import javafx.css.PseudoClass;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.PropertyBinding;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends Tile<Widget> {

  /**
   * Pseudoclass used on tiles when the widget inside loses its source.
   */
  private static final PseudoClass NO_SOURCE = PseudoClass.getPseudoClass("no-source");

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
  }
}
