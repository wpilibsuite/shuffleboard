package edu.wpi.first.shuffleboard.app.prefs;

import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.util.FxUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import org.controlsfx.control.PropertySheet;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.control.TextMatchers;

import java.util.Collections;
import java.util.Set;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@Tag("UI")
public class SettingsDialogControllerTest extends ApplicationTest {

  private SettingsDialogController controller;
  private Node root;

  @Override
  public void start(Stage stage) throws Exception {
    root = FxUtils.load(SettingsDialogController.class);
    controller = FxUtils.getController(root);
    stage.setScene(new Scene(new StackPane(root)));

    stage.show();
  }

  @Test
  public void testSingleCategory() {
    Category category = Category.of("Category",
        Group.of("Group 1",
            Setting.of("Setting 1", new SimpleBooleanProperty()),
            Setting.of("Setting 2", new SimpleBooleanProperty())
        ),
        Group.of("Group 2",
            Setting.of("Setting 3", new SimpleDoubleProperty()),
            Setting.of("Setting 4", new SimpleDoubleProperty())
        )
    );

    FxUtils.runOnFxThread(() -> controller.setRootCategories(Collections.singleton(category)));

    waitForFxEvents();

    // Check the tree view - there should only be one item
    TreeView<Category> tree = lookup(".settings-categories").query();
    ObservableList<TreeItem<Category>> children = tree.getRoot().getChildren();
    assertEquals(1, children.size(), "Only one category should be present");
    assertEquals(category, children.get(0).getValue(), "Root category was wrong");

    // Check the property sheet
    PropertySheet propertySheet = (PropertySheet) root.lookup(".property-sheet");
    assertNotNull(propertySheet, "Property sheet for category was not added");

    // Check group headers
    Set<Label> headers = lookup(".h5").queryAll();
    assertAll("Check header text",
        () -> assertEquals("Group 1", Iterables.get(headers, 0).getText(), "Different first header text"),
        () -> assertEquals("Group 2", Iterables.get(headers, 1).getText(), "Different second header text")
    );

    // Check settings editors are present
    assertAll("Check presence of editors for the settings",
        () -> assertNotNull(lookup(TextMatchers.hasText("Setting 1")).query()),
        () -> assertNotNull(lookup(TextMatchers.hasText("Setting 2")).query()),
        () -> assertNotNull(lookup(TextMatchers.hasText("Setting 3")).query()),
        () -> assertNotNull(lookup(TextMatchers.hasText("Setting 4")).query())
    );
  }

  @Test
  public void testMultipleCategories() {
    Category categoryA = Category.of("A");
    Category categoryB = Category.of("B");
    Category categoryC = Category.of("C");

    FxUtils.runOnFxThread(() -> controller.setRootCategories(ImmutableList.of(categoryA, categoryB, categoryC)));

    waitForFxEvents();

    // Should be three children
    TreeView<Category> tree = lookup(".settings-categories").query();
    ObservableList<TreeItem<Category>> children = tree.getRoot().getChildren();
    assertEquals(3, children.size(), "Three children should be present");
    assertEquals(categoryA, children.get(0).getValue());
    assertEquals(categoryB, children.get(1).getValue());
    assertEquals(categoryC, children.get(2).getValue());

    assertEquals(children.get(0), tree.getSelectionModel().getSelectedItem(), "The first child should be selected");
  }

  @Test
  public void testSubCategories() {
    Category subA1 = Category.of("Sub A1");
    Category subA2 = Category.of("Sub A2", ImmutableList.of(subA1), ImmutableList.of());
    Category rootA = Category.of("Root A", ImmutableList.of(subA2), ImmutableList.of());
    Category rootB = Category.of("Root B", ImmutableList.of(subA1), ImmutableList.of());

    FxUtils.runOnFxThread(() -> controller.setRootCategories(ImmutableList.of(rootA, rootB)));

    waitForFxEvents();

    // Category Hierarchy
    //-------------------
    // Root A
    //  - Sub A2
    //    - Sub A1
    // Root B
    //  - Sub A1

    // Should be two children
    TreeView<Category> tree = lookup(".settings-categories").query();
    ObservableList<TreeItem<Category>> children = tree.getRoot().getChildren();
    assertEquals(2, children.size(), "Two children should be present");
    assertEquals(rootA, children.get(0).getValue());
    assertEquals(rootB, children.get(1).getValue());

    // Check first root child
    TreeItem<Category> rootAItem = children.get(0);
    assertEquals(1, rootAItem.getChildren().size());
    TreeItem<Category> subA2Item = rootAItem.getChildren().get(0);
    assertEquals(subA2, subA2Item.getValue());
    assertEquals(1, subA2Item.getChildren().size());
    assertEquals(subA1, subA2Item.getChildren().get(0).getValue());

    // Check second root child
    TreeItem<Category> rootBItem = children.get(1);
    assertEquals(1, rootBItem.getChildren().size());
    assertEquals(subA1, rootBItem.getChildren().get(0).getValue());
  }

}
