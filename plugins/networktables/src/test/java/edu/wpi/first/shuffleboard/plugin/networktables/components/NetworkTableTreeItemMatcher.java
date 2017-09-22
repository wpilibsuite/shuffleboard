package edu.wpi.first.shuffleboard.plugin.networktables.components;


import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceEntry;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.function.Predicate;

import javafx.scene.control.TreeItem;

/**
 * Matchers for tree items of network table entries.
 */
public class NetworkTableTreeItemMatcher extends TypeSafeMatcher<TreeItem<NetworkTableSourceEntry>> {

  private final Predicate<TreeItem<NetworkTableSourceEntry>> predicate;
  private final String description;

  public NetworkTableTreeItemMatcher(
      Predicate<TreeItem<NetworkTableSourceEntry>> predicate, String description) {
    this.predicate = predicate;
    this.description = description;
  }

  @Override
  protected boolean matchesSafely(TreeItem<NetworkTableSourceEntry> item) {
    return predicate.test(item);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("NetworkTableSourceEntry ").appendText(this.description);
  }

  /**
   * Creates a matcher for a child item that this matcher matches.
   *
   * @param index the index of the child to match
   */
  public NetworkTableTreeItemMatcher atIndex(int index) {
    return new NetworkTableTreeItemMatcher(
        item -> item.getChildren().size() > index
            && this.matchesSafely(item.getChildren().get(index)),
        this.description + " at index " + index
    );
  }

  /**
   * Creates a matcher for a tree item with an entry that has a specific key.
   */
  public static NetworkTableTreeItemMatcher hasKey(String key) {
    return new NetworkTableTreeItemMatcher(
        item -> item.getValue().getKey().equals(key),
        "hasKey"
    );
  }

  /**
   * Creates a matcher for a tree item with an entry that has a specific simple key.
   */
  public static NetworkTableTreeItemMatcher hasSimpleKey(String simpleKey) {
    return new NetworkTableTreeItemMatcher(
        item -> item.getValue().simpleKey().equals(simpleKey),
        "hasSimpleKey"
    );
  }

  /**
   * Creates a matcher for a tree item with an entry that has a specific display string.
   */
  public static NetworkTableTreeItemMatcher hasDisplayString(String displayString) {
    return new NetworkTableTreeItemMatcher(
        item -> item.getValue().getDisplayString().equals(displayString),
        "hasDisplayString"
    );
  }

  /**
   * Creates a matcher for leaf (childless) tree items.
   */
  public static NetworkTableTreeItemMatcher isLeaf() {
    return new NetworkTableTreeItemMatcher(
        TreeItem::isLeaf,
        "isLeaf"
    );
  }

  /**
   * Creates a matcher for expanded tree items.
   */
  public static NetworkTableTreeItemMatcher isExpanded() {
    return new NetworkTableTreeItemMatcher(
        TreeItem::isExpanded,
        "isExpanded"
    );
  }

}
