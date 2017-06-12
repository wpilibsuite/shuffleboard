package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.SendableChooserData;
import edu.wpi.first.shuffleboard.sources.DataSource;

import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.Assert.assertEquals;

public class ComboBoxChooserTest extends ApplicationTest {

  private ComboBoxChooser widget;
  private SendableChooserData data;

  @Override
  public void start(Stage stage) throws Exception {
    Widgets.discover();
    widget = (ComboBoxChooser)
        Widgets.createWidget("ComboBox Chooser",
            DummySource.forTypes(DataType.SendableChooser).get()).get();
    DataSource<SendableChooserData> source = widget.getSource();
    data = source.getData();

    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @Test
  public void testOptions() {
    final String[] options = {"Foo", "Bar", "Baz"};
    data.setOptions(options);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(FXCollections.observableArrayList(options), widget.comboBox.getItems());
  }

  @Test
  public void testSelectDefaultWhenNotSelected() {
    widget.comboBox.getSelectionModel().select(null);
    WaitForAsyncUtils.waitForFxEvents();
    data.setDefaultOption("B");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("B", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testChangeToDefaultDoesNotChangeSelection() {
    data.setSelectedOption("A");
    data.setDefaultOption("B");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("A", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testSelectPreviouslySelectedValue() {
    data.setSelectedOption("C");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("C", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testSelectionChangesData() {
    Platform.runLater(() -> widget.comboBox.getSelectionModel().select("C"));
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("C", data.getSelectedOption());
  }

}
