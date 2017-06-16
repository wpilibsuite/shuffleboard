package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.function.Predicate;

import static edu.wpi.first.shuffleboard.util.TypeUtils.optionalCast;

/**
 * Represents a dashboard composed of multiple tabs.
 */
public class DashboardTabPane extends TabPane {

  public DashboardTabPane() {
    super(new DashboardTab("Tab 1"), new AdderTab());
    getTabs().stream().map(optionalCast(AdderTab.class))
            .forEach(tab -> tab.ifPresent(addTab -> addTab.setAddTabCallback(this::addNewTab)));
  }

  private void addNewTab() {
    int existingTabs = getTabs().size();
    getTabs().add(existingTabs - 1,
            new DashboardTab("Tab " + existingTabs));
  }

  /**
   * Add a widget to the active tab pane.
   * Should only be done by the result of specific user interaction.
   */
  public void addWidgetToActivePane(Widget widget) {
    optionalCast(getSelectionModel().getSelectedItem(), DashboardTab.class)
            .ifPresent(tab -> tab.getWidgetPane().addWidget(widget));
  }

  /**
   * Highlights widgets matching a predicate on all tabs.
   */
  public void selectWidgets(Predicate<Widget> selector) {
    getTabs().stream().map(optionalCast(DashboardTab.class))
            .forEach(tab -> tab.map(DashboardTab::getWidgetPane)
                    .ifPresent(pane -> pane.selectWidgets(selector)));
  }

  /**
   * Creates a single dashboard tab with the given title.
   */
  public static class DashboardTab extends Tab implements HandledTab {
    private final WidgetPane widgetPane;
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    public DashboardTab(String title) {
      super();
      this.title.set(title);
      setGraphic(new TabHandle(this));

      widgetPane = new WidgetPane();
      setContent(widgetPane);
    }

    public WidgetPane getWidgetPane() {
      return widgetPane;
    }

    @Override
    public Tab getTab() {
      return this;
    }

    @Override
    public StringProperty titleProperty() {
      return title;
    }
  }
}
