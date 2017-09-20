package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

/**
 * Represents a Tile containing a Layout.
 */
public class LayoutTile extends Tile<Layout> {
  public LayoutTile(Layout container) {
    super();
    setContent(container);
  }

  public LayoutTile(Layout component, TileSize size) {
    this(component);
    setSize(size);
  }
}
