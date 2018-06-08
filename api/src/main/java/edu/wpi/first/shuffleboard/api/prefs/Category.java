package edu.wpi.first.shuffleboard.api.prefs;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Objects;

/**
 * A category of settings. This is typically used to contain all the settings for a single configurable object, such
 * as a component, layout, or tab. Categories can have nested categories, like in the case of a layout containing
 * child components; the settings categories for the children can be nested under the settings category for the
 * parent layout. This lets components to be grouped together in a UI object.
 */
public final class Category {

  private final String name;
  private final ImmutableList<Category> subcategories;
  private final ImmutableList<Group> groups;

  /**
   * Creates a new category of settings.
   *
   * @param name   the name of the category
   * @param groups the groups of settings in this category
   *
   * @return a new category
   */
  public static Category of(String name, Group... groups) {
    return Category.of(name, ImmutableList.copyOf(groups));
  }

  /**
   * Creates a new category of settings.
   *
   * @param name   the name of the category
   * @param groups the groups of settings in this category
   *
   * @return a new category
   */
  public static Category of(String name, Collection<Group> groups) {
    return new Category(name, ImmutableList.of(), ImmutableList.copyOf(groups));
  }

  /**
   * Creates a new category of settings, with optional subcategories.
   *
   * @param name          the name of the category
   * @param subcategories the subcategories underneath this one
   * @param groups        the groups of settings in this category
   *
   * @return a new category
   */
  public static Category of(String name, Collection<Category> subcategories, Collection<Group> groups) {
    return new Category(name, ImmutableList.copyOf(subcategories), ImmutableList.copyOf(groups));
  }

  private Category(String name, ImmutableList<Category> subcategories, ImmutableList<Group> groups) {
    Objects.requireNonNull(name, "A category name cannot be null");
    if (name.chars().allMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("A category name cannot be empty");
    }
    this.name = name;
    this.subcategories = subcategories;
    this.groups = groups;
  }

  /**
   * Gets the name of this category.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the subcategories below this one.
   */
  public ImmutableList<Category> getSubcategories() {
    return subcategories;
  }

  /**
   * Gets the groups of settings in this category.
   */
  public ImmutableList<Group> getGroups() {
    return groups;
  }

}
