package edu.wpi.first.shuffleboard.util;

import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.scene.Node;

public class PseudoClassProperty extends BooleanPropertyBase {

  private final PseudoClass pseudoClass;
  private final Node node;
  private final String name;

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
