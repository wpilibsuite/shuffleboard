package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;

/**
 * Represents a tile containing a widget.
 */
public class WidgetTile extends Tile<Widget> {

  /**
   * Pseudoclass used on tiles when the widget inside loses its source.
   */
  private static final PseudoClass NO_SOURCE = PseudoClass.getPseudoClass("no-source");

  // Store as a field to prevent GC
  private MonadicBinding<ObservableList<DataSource>> retained; //NOPMD could be a local variable

  private final InvalidationListener dataSourceListChangeListener = __ -> {
    if (retained.get() != null && retained.get().stream().anyMatch(s -> s instanceof DestroyedSource)) {
      pseudoClassStateChanged(NO_SOURCE, true);
    } else {
      pseudoClassStateChanged(NO_SOURCE, false);
    }
  };

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

    retained = EasyBind.monadic(contentProperty()).map(Sourced::getSources);

    retained.addListener((observable, prev, cur) -> {
      if (prev != null) {
        prev.removeListener(dataSourceListChangeListener);
      }
      if (cur != null) {
        cur.addListener(dataSourceListChangeListener);
      }
      dataSourceListChangeListener.invalidated(observable);
    });
  }
}
