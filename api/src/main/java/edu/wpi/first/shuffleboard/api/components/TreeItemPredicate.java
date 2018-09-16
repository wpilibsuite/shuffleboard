package edu.wpi.first.shuffleboard.api.components;

import java.util.function.Predicate;

import javafx.scene.control.TreeItem;

@FunctionalInterface
public interface TreeItemPredicate<T> {

  TreeItemPredicate ALWAYS = (parent, value) -> true;

  TreeItemPredicate NEVER = (parent, value) -> false;

  boolean test(TreeItem<T> parent, T value);

  static <T> TreeItemPredicate<T> always() {
    return ALWAYS;
  }

  static <T> TreeItemPredicate<T> never() {
    return NEVER;
  }

  static <T> TreeItemPredicate<T> fromPredicate(Predicate<? super T> predicate) {
    return (parent, value) -> predicate.test(value);
  }

}
