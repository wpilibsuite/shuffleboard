package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

public class PluginPaneController {

  private static final Logger log = Logger.getLogger(PluginPaneController.class.getName());

  @FXML
  private Pane root;
  @FXML
  private TableView<Plugin> pluginTable;

  private final TableColumn<Plugin, String> nameColumn = new TableColumn<>("Plugin");
  private final TableColumn<Plugin, Boolean> loadedColumn = new TableColumn<>("Loaded");

  @FXML
  private void initialize() {
    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getName()));
    final ObservableList<Plugin> loadedPlugins = PluginLoader.getDefault().getKnownPlugins();
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
    loadedColumn.setEditable(true);
    loadedColumn.setCellFactory(param -> new CheckBoxTableCell<>());
    pluginTable.getColumns().addAll(nameColumn, loadedColumn);
    pluginTable.setItems(loadedPlugins);
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
        } catch (IOException e) {
          log.log(Level.WARNING, "Could not load jar", e);
        }
      }
    });
  }

}
