package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.app.widget.Layout;

public class LayoutTile extends Tile<Layout> {
  public LayoutTile(Layout container) {
    super();
    setContent(container);
    System.out.println("cc " + container.getView() + ":" + getCenter());
  }
}
