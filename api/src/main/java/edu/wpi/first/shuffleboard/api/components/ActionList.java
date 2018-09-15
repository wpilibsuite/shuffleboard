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

/**
 * <p>A class meant to represent abstract "actions" that can be taken by a user for a given context.</p>
 *
 * <p>The most common use-case is currently to add ActionList Suppliers to the scene graph, so that all
 * nodes under the mouse cursor can add actions to a right-click event.</p>
 *
 * <p>The name of an ActionList should be descriptive, as it will be displayed to the user, and reasonably
 * unique within its context--ActionLists with the same name should be combined by the consumer into
 * a single Menu.</p>
 */
public class ActionList {
  private static final Object ACTION_LIST_KEY = new Object();

  private final String name;
  private final List<Supplier<MenuItem>> actions;

  public static final class Action {
    private final String name;
    private final Runnable action;
    private final Node graphic;

    private Action(String name, Runnable action, Node graphic) {
      this.name = name;
      this.action = action;
      this.graphic = graphic;
    }

    /**
     * Creates an action.
     *
     * @param name   the name of the action
     * @param action the code that the action should run
     *
     * @return a new action
     */
    public static Action of(String name, Runnable action) {
      return new Action(name, action, null);
    }

    /**
     * Creates an action.
     *
     * @param name    the name of the action
     * @param action  the code that the action should run
     * @param graphic an optional graphic that should be displayed in the action's menu. If null, no graphic will be
     *                displayed
     *
     * @return a new action
     */
    public static Action of(String name, Runnable action, Node graphic) {
      return new Action(name, action, graphic);
    }

    /**
     * Gets the name of this action.
     */
    public String getName() {
      return name;
    }

    /**
     * Gets the code that this action runs.
     */
    public Runnable getAction() {
      return action;
    }

    /**
     * Gets the graphic used to display this action.
     */
    public Node getGraphic() {
      return graphic;
    }

    /**
     * Creates a new menu item for this action.
     */
    public MenuItem asMenuItem() {
      MenuItem menuItem = FxUtils.menuItem(name, __ -> action.run());
      menuItem.setGraphic(graphic);
      return menuItem;
    }

  }

  protected ActionList(String name) {
    this.name = name;
    this.actions = new ArrayList<>();
  }

  public boolean hasItems() {
    return !actions.isEmpty();
  }

  /**
   * Creates a new action. This is shorthand for {@link Action#of(String, Runnable)}.
   */
  public static Action createAction(String name, Runnable action) {
    return Action.of(name, action);
  }

  /**
   * Creates a new action. This is shorthand for {@link Action#of(String, Runnable, Node)}.
   */
  public static Action createAction(String name, Runnable action, Node graphic) {
    return Action.of(name, action, graphic);
  }

  public ActionList addAction(Action action) {
    actions.add(action::asMenuItem);
    return this;
  }

  public ActionList addAction(String name, Runnable r) {
    return addAction(Action.of(name, r));
  }

  /**
   * Add an action with an associated graphic, such as a checkmark or icon.
   */
  public ActionList addAction(String name, Node graphic, Runnable r) {
    return addAction(Action.of(name, r, graphic));
  }

  /**
   * Returns {@link MenuItem} view of the ActionList, with all items represented by either text items or sub-menus.
   */
  public List<MenuItem> toMenuItems() {
    return actions.stream().map(Supplier::get).collect(Collectors.toList());
  }

  private Menu asMenu() {
    Menu menu = new Menu(name);
    actions.stream().map(Supplier::get).forEach(mi -> menu.getItems().add(mi));
    return menu;
  }

  /**
   * Adds another action list to this one, placing each of its actions in a separate sub-menu.
   *
   * <p>For example, adding an action list "C" with actions named "C1" and "C2" to another action list with actions
   * "A" and "B" would look something like this:
   * <pre>
   *   - A
   *   - B
   *   - C
   *     - C1
   *     - C2
   * </pre></p>
   */
  public ActionList addNested(ActionList al) {
    actions.add(al::asMenu);
    return this;
  }

  public String getName() {
    return name;
  }

  /**
   * Creates an empty action list with the given name.
   *
   * @param name the name of the new action list
   */
  public static ActionList withName(String name) {
    return new ActionList(name);
  }

  /**
   * Add an ActionList supplier to a Node, for insertion in a scene graph.
   */
  public static void registerSupplier(Node node, Supplier<ActionList> supplier) {
    node.getProperties().put(ACTION_LIST_KEY, supplier);
  }

  /**
   * Retrieves the ActionList supplier that has been added to a node, if one exists.
   */
  public static Optional<ActionList> actionsForNode(Node node) {
    return Optional.ofNullable(node.getProperties().get(ACTION_LIST_KEY))
        .flatMap(TypeUtils.optionalCast(Supplier.class))
        .map(Supplier::get)
        .flatMap(TypeUtils.optionalCast(ActionList.class));
  }
}
