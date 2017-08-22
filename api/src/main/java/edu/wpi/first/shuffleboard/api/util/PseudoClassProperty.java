package edu.wpi.first.shuffleboard.api.util;

import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Node;

/**
 * A boolean property that is reflected as a pseudo-class selector on the given node.
 */
public class PseudoClassProperty extends BooleanPropertyBase {

  private final PseudoClass pseudoClass;
  private final Node node;
  private final String name;

  /**
   * Creates a property for the specific node and pseudoClass.
   */
  public PseudoClassProperty(Node node, String pseudoClass) {
    this.node = node;
    this.name = pseudoClass;
    this.pseudoClass = PseudoClass.getPseudoClass(pseudoClass);
  }

  @Override
  protected void invalidated() {
    this.node.pseudoClassStateChanged(pseudoClass, get());
  }

  @Override
  public Object getBean() {
    return node;
  }

  @Override
  public String getName() {
    return name;
  }
}
