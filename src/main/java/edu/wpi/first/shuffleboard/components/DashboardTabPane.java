package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.function.Predicate;

import static edu.wpi.first.shuffleboard.util.TypeUtils.optionalCast;

public class DashboardTabPane extends TabPane {
  public DashboardTabPane() {
    super(new DashboardTab("Tab 1"), new AdderTab());
    getTabs().stream().map(optionalCast(AdderTab.class))
            .forEach(tab -> tab.ifPresent(addTab -> addTab.setOnAddTab(this::addNewTab)));
  }

  private void addNewTab() {
    int existingTabs = getTabs().size();
    getTabs().add(existingTabs - 1,
            new DashboardTab("Tab "+ existingTabs));
  }

  public void addManually(Widget widget) {
    optionalCast(getSelectionModel().getSelectedItem(), DashboardTab.class)
            .ifPresent(tab -> tab.getWidgetPane().addWidget(widget));
  }

  public void selectWidgets(Predicate<Widget> selector) {
    getTabs().stream().map(optionalCast(DashboardTab.class))
            .forEach(tab -> tab.map(DashboardTab::getWidgetPane)
                    .ifPresent(pane -> pane.selectWidgets(selector)));
  }

  public static class DashboardTab extends Tab {
    final private WidgetPane widgetPane;

    public DashboardTab(String title) {
      super(title);
      widgetPane = new WidgetPane();
      setContent(widgetPane);
    }

    public WidgetPane getWidgetPane() {
      return widgetPane;
    }
  }

}
