package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class PluginPaneController {

  private static final Logger log = Logger.getLogger(PluginPaneController.class.getName());

  @FXML
  private Pane root;
  @FXML
  private SplitPane splitPane;
  @FXML
  private TableView<Plugin> pluginTable;
  @FXML
  private TableColumn<Plugin, String> nameColumn;
  @FXML
  private TableColumn<Plugin, Boolean> loadedColumn;
  @FXML
  private TableColumn<Plugin, String> versionColumn;
  @FXML
  private TextArea descriptionArea;

  @FXML
  private void initialize() {
    splitPane.setDividerPositions(0.75);
    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().idString()));
    versionColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getVersion()));
    loadedColumn.setCellValueFactory(param -> {
      Plugin plugin = param.getValue();
      SimpleBooleanProperty prop = new SimpleBooleanProperty(true);
      prop.addListener((__, was, is) -> {
        if (was) {
          PluginLoader.getDefault().unload(plugin);
        } else {
          PluginLoader.getDefault().load(plugin);
        }
      });
      return prop;
    });
    loadedColumn.setCellFactory(param -> new CheckBoxTableCell<>()); //TODO use toggle switches
    pluginTable.setItems(PluginLoader.getDefault().getKnownPlugins());
    pluginTable.getItems().addListener((ListChangeListener<Plugin>) c -> {
      if (pluginTable.getSelectionModel().getSelectedItem() == null) {
        pluginTable.getSelectionModel().select(0);
      }
    });
    MonadicBinding<String> desc = EasyBind.monadic(pluginTable.selectionModelProperty())
        .flatMap(SelectionModel::selectedItemProperty)
        .map(this::createPluginDetailString);
    descriptionArea.textProperty().bind(desc);
  }

  private String createPluginDetailString(Plugin plugin) {
    // TODO better text formatting and styles
    return "Plugin: " + plugin.getName()
        + "\nGroup ID: " + plugin.getGroupId()
        + "\nVersion: " + plugin.getVersion()
        + "\n\n" + plugin.getDescription();
  }

  @FXML
  private void loadPlugin() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choose a plugin");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plugin", "*.class", "*.jar"));
    List<File> files = chooser.showOpenMultipleDialog(root.getScene().getWindow());
    if (files == null) {
      return;
    }
    files.forEach(f -> {
      if (f.getName().endsWith(".class")) { //NOPMD empty if statement
        //TODO
      } else if (f.getName().endsWith(".jar")) {
        try {
          JarFile jar = new JarFile(f);
          PluginLoader.getDefault().loadPluginJar(jar);
        } catch (IOException | IllegalArgumentException e) {
          // TODO improve the dialog; use something like what GRIP has
          Alert alert = new Alert(Alert.AlertType.ERROR, null, ButtonType.OK);
          alert.setTitle("Could not load plugins");
          alert.setHeaderText("Plugins in " + f.getName() + " could not be loaded");
          alert.setContentText("Error message:\n\n    " + e.getMessage());
          alert.showAndWait();
          log.log(Level.WARNING, "Could not load jar", e);
        }
      }
    });
  }

}
