package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.widget.Size;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

import static edu.wpi.first.shuffleboard.widget.DataType.Boolean;
import static edu.wpi.first.shuffleboard.widget.DataType.Number;
import static edu.wpi.first.shuffleboard.widget.DataType.Text;

/**
 * A helper class that defines stock widgets.
 */
public final class StockWidgets {

  /**
   * Initializes and registers all stock widgets.
   */
  public static void init() {

    // Text view
    Widget.simpleWidget(widget -> {
      widget.setName("Text View");
      DataSource<Object> source = widget.getSource();
      widget.supportDataTypes(Text, Number, Boolean);
      widget.setPreferredSize(new Size(2, 1));
      TextField textField = new TextField();
      textField.textProperty().bind(Bindings.createStringBinding(() -> source.getData() == null ? "" : source.getData().toString(),
                                                                 source.dataProperty()));
      widget.addView(new Size(1, 1), stackPane -> {
        stackPane.setPadding(new Insets(8));
        stackPane.getChildren().add(textField);
      });
      widget.addView(new Size(2, 1), BorderPane::new, borderPane -> {
        borderPane.setPadding(new Insets(8));
        borderPane.setTop(new Label(source.getName()));
        borderPane.setCenter(textField);
      });
    });
  }

}
