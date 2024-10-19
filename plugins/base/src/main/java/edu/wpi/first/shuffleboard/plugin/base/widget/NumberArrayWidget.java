package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.control.ArrayTableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.stream.IntStream;

@Description(
        name = "Number Array",
        dataTypes = double[].class,
        summary = "Displays an array of numbers"
)
public final class NumberArrayWidget extends SimpleAnnotatedWidget<double[]> {
    private final StackPane pane = new StackPane();
    private final ArrayTableView<Double> table = new ArrayTableView<>();

    public NumberArrayWidget() {
        pane.getChildren().add(table);

        dataOrDefault.addListener((observableValue, oldDoubles, newDoubles) -> {
            final var array = new Double[newDoubles.length];
            IntStream.range(0, newDoubles.length).forEach(i -> array[i] = newDoubles[i]);
            table.setItems(array);
        });
    }

    @Override
    public Pane getView() {
        return pane;
    }
}