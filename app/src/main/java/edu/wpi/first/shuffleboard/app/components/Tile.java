package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.util.PropertyUtils;
import edu.wpi.first.shuffleboard.api.util.PseudoClassProperty;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.fxmisc.easybind.monadic.PropertyBinding;

import java.io.IOException;
import java.util.Optional;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

/**
 * Contains any component directly embedded in a WidgetPane. Has a size, content, and title.
 */
public class Tile<T extends Component> extends BorderPane {

  private final Property<T> content = new SimpleObjectProperty<>(this, "content", null);
  private final MonadicBinding<Pane> contentView = EasyBind.monadic(content).map(Component::getView); // NOPMD

  private final Property<TileSize> size = new SimpleObjectProperty<>(this, "size", null);
  private final BooleanProperty selected = new PseudoClassProperty(this, "selected");

  private final PropertyBinding<String> contentTitle = // NOPMD local variable
      EasyBind.monadic(content)
          .selectProperty(Component::titleProperty);

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
    EditableLabel editableLabel = (EditableLabel) lookup("#titleLabel");
    editableLabel.textProperty().bindBidirectional(contentTitle);
    ((Label) lookup("#titleLabel").lookup(".label")).setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
    contentView.addListener((__, oldContent, newContent) -> {
      getContentPane()
          .map(Pane::getChildren)
          .ifPresent(c -> {
            if (newContent == null) {
              c.clear();
            } else {
              c.setAll(newContent);
            }
          });
    });
    contentTitle.addListener((__, prev, cur) -> editableLabel.setText(cur));
  }

  private Optional<Pane> getContentPane() {
    return Optional.ofNullable((Pane) lookup("#contentPane"));
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

  /**
   * Create a tile for an arbitrary component.
   */
  public static <C extends Component> Tile<C> tileFor(C component, TileSize size) {
    if (component instanceof Widget) {
      return (Tile<C>) new WidgetTile((Widget) component, size);
    } else if (component instanceof Layout) {
      return (Tile<C>) new LayoutTile((Layout) component, size);
    } else {
      Tile<C> tile = new Tile<>();
      tile.setContent(component);
      tile.setSize(size);
      return tile;
    }
  }
}
