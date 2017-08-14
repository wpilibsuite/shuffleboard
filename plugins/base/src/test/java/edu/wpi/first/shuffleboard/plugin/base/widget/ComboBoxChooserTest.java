package edu.wpi.first.shuffleboard.plugin.base.widget;


import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.widget.Widgets;
import edu.wpi.first.shuffleboard.plugin.base.data.SendableChooserData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SendableChooserType;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ComboBoxChooserTest extends ApplicationTest {

  private ComboBoxChooser widget;
  private DataSource<SendableChooserData> source;

  @Override
  public void start(Stage stage) throws Exception {
    widget = (ComboBoxChooser)
        Widgets.getDefault().createWidget("ComboBox Chooser",
            DummySource.forTypes(new SendableChooserType()).get()).get();
    source = widget.getSource();

    stage.setScene(new Scene(widget.getView()));
    stage.show();
  }

  @Test
  public void testOptions() {
    final String[] options = {"Foo", "Bar", "Baz"};
    SendableChooserData data = source.getData().withOptions(options);
    source.setData(data);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(FXCollections.observableArrayList(options), widget.comboBox.getItems());
  }

  @Test
  public void testSelectDefaultWhenNotSelected() {
    widget.comboBox.getSelectionModel().select(null);
    WaitForAsyncUtils.waitForFxEvents();
    SendableChooserData data = source.getData().withDefaultOption("B");
    source.setData(data);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("B", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testChangeToDefaultDoesNotChangeSelection() {
    SendableChooserData data = source.getData().withSelectedOption("A").withDefaultOption("B");
    source.setData(data);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("A", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testSelectPreviouslySelectedValue() {
    SendableChooserData data = source.getData().withSelectedOption("C");
    source.setData(data);
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("C", widget.comboBox.getSelectionModel().getSelectedItem());
  }

  @Test
  public void testSelectionChangesData() {
    Platform.runLater(() -> widget.comboBox.getSelectionModel().select("C"));
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals("C", source.getData().getSelectedOption());
  }

}
