package edu.wpi.first.shuffleboard.api.tab.model;

@FunctionalInterface
public interface StructureChangeListener {

  void structureChanged(TabStructure structure);

}
