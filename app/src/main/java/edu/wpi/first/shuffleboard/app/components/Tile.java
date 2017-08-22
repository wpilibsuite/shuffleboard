package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.util.PropertyUtils;
import edu.wpi.first.shuffleboard.api.util.PseudoClassProperty;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

import org.fxmisc.easybind.EasyBind;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

public abstract class Tile<T extends Component> extends BorderPane {

  private final Property<T> content = new SimpleObjectProperty<>(this, "content", null);

  private final Property<TileSize> size = new SimpleObjectProperty<>(this, "size", null);
  private final BooleanProperty selected = new PseudoClassProperty(this, "selected");

  /**
   * Creates an empty tile. The content and size must be set with {@link #setContent(T)} and
   * {@link #setSize(TileSize)}.
   */
  protected Tile() {
    try {
      FXMLLoader loader = new FXMLLoader(Tile.class.getResource("Tile.fxml"));
      loader.setRoot(this);
      loader.load();
    } catch (IOException e) {
      throw new RuntimeException("Could not load the widget tile FXML", e);
    }

    getStyleClass().addAll("tile", "card");
    PropertyUtils.bindWithConverter(idProperty(), contentProperty(), w -> "tile[" + w + "]");
    ((EditableLabel) lookup("#titleLabel")).textProperty().bindBidirectional(
        EasyBind.monadic(contentProperty()).selectProperty(Component::titleProperty)
    );
    centerProperty().bind(EasyBind.monadic(contentProperty()).map(Component::getView));
  }

  public final T getContent() {
    return content.getValue();
  }

  public final Property<T> contentProperty() {
    return content;
  }

  /**
   * Sets the content for this tile. This tile will update to show the view for the given content;
   * however, the tile will not change size. The size must be set separately with
   * {@link #setSize(TileSize)}.
   */
  public final void setContent(T content) {
    this.content.setValue(content);
  }

  public final TileSize getSize() {
    return size.getValue();
  }

  public final Property<TileSize> sizeProperty() {
    return size;
  }

  /**
   * Sets the size of this tile. This does not directly change the size of the tile; it is up to
   * the parent pane to actually resize this tile.
   */
  public final void setSize(TileSize size) {
    this.size.setValue(size);
  }

  public boolean isSelected() {
    return selected.get();
  }

  public void setSelected(boolean value) {
    selected.set(value);
  }

  public BooleanProperty selectedProperty() {
    return selected;
  }
}
