package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.control.ArrayTableView;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.IntStream;

@Description(
        name = "Boolean Array",
        dataTypes = boolean[].class,
        summary = "Displays an array of booleans"
)
public final class BooleanArrayWidget extends SimpleAnnotatedWidget<boolean[]> {
    private final StackPane pane = new StackPane();
    private final ArrayTableView<Boolean> table = new ArrayTableView<>();

    private final Property<Color> trueColor
            = new SimpleObjectProperty<>(this, "colorWhenTrue", Color.LAWNGREEN);
    private final Property<Color> falseColor
            = new SimpleObjectProperty<>(this, "colorWhenFalse", Color.DARKRED);

    public BooleanArrayWidget() {
        pane.getChildren().add(table);

        table.setValueCellFactory(tableColumn -> new BooleanTableCell<>());

        dataOrDefault.addListener((observableValue, oldBooleans, newBooleans) -> {
            final var array = new Boolean[newBooleans.length];
            IntStream.range(0, newBooleans.length).forEach(i -> array[i] = newBooleans[i]);
            table.setItems(array);
        });
    }

    @Override
    public List<Group> getSettings() {
        return List.of(
                Group.of("Colors",
                        Setting.of("Color when true", "The color to use when a value is `true`", trueColor, Color.class),
                        Setting.of("Color when false", "The color to use when a value is `false`", falseColor, Color.class)
                )
        );
    }

    @Override
    public Pane getView() {
        return pane;
    }

    private class BooleanTableCell<S> extends TableCell<S, Boolean> {
        private Background createBooleanBackground(boolean value) {
            return new Background(new BackgroundFill(value ? trueColor.getValue() : falseColor.getValue(), null, null));
        }

        @Override
        protected void updateItem(Boolean t, boolean empty) {
            if (empty || t == null)
                setBackground(null);
            else
                setBackground(createBooleanBackground(t));
        }
    }
}