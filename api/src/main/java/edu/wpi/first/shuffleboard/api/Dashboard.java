package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.function.Predicate;

public interface Dashboard {

  /**
   * Adds the given widget to the active widget pane.
   */
  void addWidgetToActivePane(Widget widget);

  /**
   * Selects all widgets in all dashboard tabs that match the given selector.
   */
  void selectWidgets(Predicate<Widget> selector);

}
