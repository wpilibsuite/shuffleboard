package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class ActionList {
  private final String name;
  private final List<Supplier<MenuItem>> actions;

  private ActionList(String name) {
    this.name = name;
    this.actions = new ArrayList<>();
  }

  public ActionList addAction(String name, Runnable r) {
    actions.add(() -> FxUtils.menuItem(name, _e -> r.run()));
    return this;
  }

  public boolean hasItems() {
    return actions.size() > 0;
  }

  /**
   * Add an action with an associated graphic, such as a checkmark or icon.
   */
  public ActionList addAction(String name, Node graphic, Runnable r) {
    actions.add(() -> {
      MenuItem i = FxUtils.menuItem(name, _e -> r.run());
      i.setGraphic(graphic);
      return i;
    });
    return this;
  }

  /**
   * @return a {@link MenuItem} view of the ActionList, with all items represented by either text items or sub-menus.
   */
  public List<MenuItem> toMenuItems() {
    return actions.stream().map(Supplier::get).collect(Collectors.toList());
  }

  private Menu asMenu() {
    Menu menu = new Menu(name);
    actions.stream().map(Supplier::get).forEach(mi -> menu.getItems().add(mi));
    return menu;
  }

  public ActionList addNested(ActionList al) {
    actions.add(al::asMenu);
    return this;
  }

  public String getName() {
    return name;
  }

  public static ActionList withName(String name) {
    return new ActionList(name);
  }

  private static Object ACTION_LIST_KEY = new Object();

  /**
   * Add an ActionList supplier to a Node, for insertion in a scene graph.
   */
  public static void registerSupplier(Node node, Supplier<ActionList> supplier) {
    node.getProperties().put(ACTION_LIST_KEY, supplier);
  }

  /**
   * Retrieves the ActionList supplier that has been added to a node, if one exists.
   */
  public static Optional<Supplier<ActionList>> getSupplier(Node node) {
    return Optional.ofNullable(node.getProperties().get(ACTION_LIST_KEY))
        .flatMap(TypeUtils.optionalCast(Supplier.class))
        .map(s -> (Supplier<ActionList>) () -> (ActionList) s.get());
  }
}
