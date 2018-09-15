package edu.wpi.first.shuffleboard.api.components;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Custom skin for {@link ExtendedPropertySheet}. This is very similar to the default skin, but has a different display
 * for {@link PropertySheet.Mode#CATEGORY} that displays categories with static headings instead of an accordion pane.
 */
@SuppressWarnings("PMD.DefaultPackage")
class ExtendedPropertySheetSkin extends SkinBase<ExtendedPropertySheet> {

  /* Not private to avoid generated accessor methods */
  static final double MIN_COLUMN_WIDTH = 100;
  private final BorderPane content = new BorderPane();
  private final ScrollPane scrollPane = new ScrollPane();

  ExtendedPropertySheetSkin(ExtendedPropertySheet skinnable) {
    super(skinnable);
    scrollPane.setFitToWidth(true);
    content.setCenter(scrollPane);
    getChildren().add(content);

    skinnable.getItems().addListener((InvalidationListener) observable -> update());
    skinnable.modeProperty().addListener(__ -> update());
    skinnable.propertyEditorFactory().addListener(__ -> update());

    update();
  }

  @Override
  protected void layoutChildren(double x, double y, double w, double h) {
    content.resizeRelocate(x, y, w, h);
  }

  private void update() {
    scrollPane.setContent(createPropertySheetContainer());
  }

  private Node createPropertySheetContainer() {
    switch (getSkinnable().getMode()) {
      case CATEGORY:
        VBox container = new VBox();
        container.setSpacing(5);
        container.setFillWidth(true);
        Multimap<String, PropertySheet.Item> categories = LinkedListMultimap.create();
        for (PropertySheet.Item item : getSkinnable().getItems()) {
          categories.put(item.getCategory(), item);
        }
        int[] row = {0};
        PropertyPane pane = new PropertyPane(Collections.emptyList());
        categories.asMap().forEach((group, items) -> {
          Label label = new Label(group);
          label.getStyleClass().add("h5");
          VBox box = new VBox(label, new Separator());
          GridPane.setMargin(box, new Insets(15, 0, 0, 0));
          box.setFillWidth(true);
          pane.add(box, 0, row[0], 2, 1);
          row[0]++;
          for (PropertySheet.Item item : items) {
            addEditorForItem(pane, row[0], item);
            row[0]++;
          }
        });
        container.getChildren().add(pane);
        return container;
      case NAME:
        // Fallthrough to sort by name, with no group headings
      default:
        return new PropertyPane(getSkinnable().getItems());
    }
  }

  static void addEditorForItem(PropertyPane propertyPane, int row, PropertySheet.Item item) {
    // setup property label
    Label label = new Label(item.getName());
    label.setMinWidth(MIN_COLUMN_WIDTH);
    GridPane.setMargin(label, new Insets(0, 0, 0, 15));

    // show description as a tooltip
    String description = item.getDescription();
    if (description != null && !description.chars().allMatch(Character::isWhitespace)) {
      label.setTooltip(new Tooltip(description));
    }

    propertyPane.add(label, 0, row);

    // setup property editor
    Node editor = propertyPane.getEditor(item);

    if (editor instanceof Region && !(editor instanceof ToggleSwitch)) {
      ((Region) editor).setMinWidth(MIN_COLUMN_WIDTH);
      ((Region) editor).setMaxWidth(Double.MAX_VALUE);
    }
    label.setLabelFor(editor);
    propertyPane.add(editor, 1, row);

    if (!(editor instanceof ToggleSwitch)) {
      // Toggle switches with HGrow set are placed to the far right of the pane, putting them very far
      // away from the name of the item they edit.
      GridPane.setHgrow(editor, Priority.ALWAYS);
    }
  }

  final class PropertyPane extends GridPane {

    public PropertyPane(Collection<PropertySheet.Item> properties) {
      this(properties, 0);
    }

    public PropertyPane(Collection<PropertySheet.Item> properties, int nestingLevel) {
      setVgap(5);
      setHgap(5);
      setPadding(new Insets(5, 15, 5, 15 + nestingLevel * 10));
      getStyleClass().add("property-pane");
      setItems(properties);
      getColumnConstraints().addAll(
          new ColumnConstraints(MIN_COLUMN_WIDTH, MIN_COLUMN_WIDTH * 1.5, Double.MAX_VALUE),
          new ColumnConstraints(MIN_COLUMN_WIDTH, MIN_COLUMN_WIDTH, Double.MAX_VALUE)
      );
      setMinWidth(MIN_COLUMN_WIDTH * 2.1);
      setPrefWidth(MIN_COLUMN_WIDTH * 2.6);
    }

    public void setItems(Collection<PropertySheet.Item> properties) {
      getChildren().clear();

      String filter = getSkinnable().titleFilter().get();
      filter = filter == null ? "" : filter.trim().toLowerCase(Locale.getDefault());

      int row = 0;

      for (PropertySheet.Item item : properties) {

        // filter properties
        String title = item.getName();

        if (!filter.isEmpty() && !title.toLowerCase(Locale.getDefault()).contains(filter)) {
          continue;
        }

        addEditorForItem(this, row, item);

        row++;
      }

    }

    @SuppressWarnings("unchecked")
    Node getEditor(PropertySheet.Item item) {
      PropertyEditor editor = getSkinnable().getPropertyEditorFactory().call(item);
      if (editor == null) {
        editor = new DefaultEditor(item);
      } else if (!item.isEditable()) {
        editor.getEditor().setDisable(true);
      }
      editor.setValue(item.getValue());
      return editor.getEditor();
    }

    /**
     * The default editor for properties whose types do not have an editor provided by the property sheet's
     * {@link PropertySheet#propertyEditorFactory() editor factory}. This editor is read-only.
     */
    private class DefaultEditor extends AbstractPropertyEditor<Object, TextField> {

      public DefaultEditor(PropertySheet.Item item) {
        super(item, new TextField(), true);
        getEditor().setEditable(false);
        getEditor().setDisable(true);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected ObservableValue<Object> getObservableValue() {
        return (ObservableValue<Object>) (Object) getEditor().textProperty();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void setValue(Object value) {
        getEditor().setText(value == null ? "" : value.toString()); //$NON-NLS-1$
      }
    }
  }
}
