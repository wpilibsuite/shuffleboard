package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.sources.DataSource;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.Assert.assertEquals;

public class ComboBoxChooserTest extends ApplicationTest {

  private ComboBoxChooser widget;
  private ObservableMap<String, Object> map;

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
    widget = (ComboBoxChooser)
        Widgets.createWidget("ComboBox Chooser",
            DummySource.forTypes(DataType.SendableChooser).get()).get();
    DataSource<ObservableMap<String, Object>> source = widget.getSource();
    map = source.getData();

    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @Test
  public void testOptions() {
    final String[] options = {"Foo", "Bar", "Baz"};
    Platform.runLater(() -> map.put(ComboBoxChooser.OPTIONS_KEY, options));
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(FXCollections.observableArrayList(options), widget.comboBox.getItems());
  }

  @Test
  public void testSelectDefaultWhenNotSelected() {
    widget.comboBox.getSelectionModel().select(null);
    WaitForAsyncUtils.waitForFxEvents();
    map.put(ComboBoxChooser.DEFAULT_VALUE_KEY, "B");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("B", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testChangeToDefaultDoesNotChangeSelection() {
    map.put(ComboBoxChooser.DEFAULT_VALUE_KEY, "B");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("A", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testSelectPreviouslySelectedValue() {
    Platform.runLater(() -> map.put(ComboBoxChooser.SELECTED_VALUE_KEY, "C"));
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("C", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testSelectionChangesData() {
    Platform.runLater(() -> widget.comboBox.getSelectionModel().select("C"));
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("C", map.get(ComboBoxChooser.SELECTED_VALUE_KEY));
  }

}
