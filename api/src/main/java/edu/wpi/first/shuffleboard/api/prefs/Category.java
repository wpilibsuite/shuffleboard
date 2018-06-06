package edu.wpi.first.shuffleboard.api.prefs;

import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;

import com.google.common.collect.ImmutableList;

import org.controlsfx.control.PropertySheet;

import java.util.Collection;
import java.util.Optional;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

public final class Category {

  private final String name;
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
    return new Category(name, ImmutableList.copyOf(groups));
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
    return new Category(name, ImmutableList.copyOf(groups));
  }

  private Category(String name, ImmutableList<Group> groups) {
    this.name = name;
    this.groups = groups;
  }

  /**
   * Gets the name of this category.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the groups of settings in this category.
   */
  public ImmutableList<Group> getGroups() {
    return groups;
  }

  /**
   * Creates a property sheet for editing the settings in this category.
   *
   * @return a new property sheet for this category
   */
  public PropertySheet createPropertySheet() {
    ExtendedPropertySheet propertySheet = new ExtendedPropertySheet();
    propertySheet.setMode(PropertySheet.Mode.CATEGORY);
    for (Group group : groups) {
      for (Setting<?> setting : group.getSettings()) {
        PropertySheet.Item item = new PropertySheet.Item() {
          @Override
          public Class<?> getType() {
            return setting.getProperty().getValue().getClass();
          }

          @Override
          public String getCategory() {
            return group.getName();
          }

          @Override
          public String getName() {
            return setting.getName();
          }

          @Override
          public String getDescription() {
            return setting.getDescription();
          }

          @Override
          public Object getValue() {
            return setting.getProperty().getValue();
          }

          @Override
          @SuppressWarnings("unchecked")
          public void setValue(Object value) {
            ((Property) setting.getProperty()).setValue(value);
          }

          @Override
          public Optional<ObservableValue<?>> getObservableValue() {
            return Optional.of(setting.getProperty());
          }
        };
        propertySheet.getItems().add(item);
      }
    }
    return propertySheet;
  }

}
