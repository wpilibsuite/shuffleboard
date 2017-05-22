package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.widget.Size;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

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

    // Toggle button
    Widget.<Boolean>simpleWidget(widget -> {
      widget.setName("Toggle Button");
      DataSource<Boolean> source = widget.getSource();
      widget.supportDataTypes(Boolean);
      ToggleButton button = new ToggleButton();
      button.textProperty().bind(source.nameProperty());
      button.selectedProperty().bindBidirectional(source.dataProperty());

      widget.addView(new Size(1, 1), stackPane -> {
        stackPane.setPadding(new Insets(8));
        stackPane.getChildren().add(button);
      });
    });

    // Boolean box
    Widget.<Boolean>simpleWidget(widget -> {
      widget.setName("Boolean Box");
      DataSource<Boolean> source = widget.getSource();
      widget.supportDataTypes(Boolean);

      // Ideally these would be properties of the widget that can can be changed in the UI
      Color onFalse = Color.DARKRED;
      Color onTrue = Color.GREEN;
      widget.addView(new Size(1, 1), stackPane -> {
        stackPane.backgroundProperty()
                 .bind(FxUtils.when(source.dataProperty())
                              .then(new Background(new BackgroundFill(onTrue, null, null)))
                              .otherwise(new Background(new BackgroundFill(onFalse, null, null))));
      });
    });

  }

}
