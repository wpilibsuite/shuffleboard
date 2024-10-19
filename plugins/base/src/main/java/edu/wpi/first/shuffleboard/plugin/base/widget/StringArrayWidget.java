package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.control.ArrayTableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

@Description(
    name = "String Array",
    dataTypes = String[].class,
    summary = "Displays an array of strings"
)
public final class StringArrayWidget extends SimpleAnnotatedWidget<String[]> {
  private final StackPane pane = new StackPane();
  private final ArrayTableView<String> table = new ArrayTableView<>();

  @SuppressWarnings("JavadocMethod")
  public StringArrayWidget() {
    pane.getChildren().add(table);

    dataOrDefault.addListener((observableValue, oldStrings, newStrings) -> table.setItems(newStrings));
  }

  @Override
  public Pane getView() {
    return pane;
  }
}