package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.app.plugin.PluginCache;
import edu.wpi.first.shuffleboard.app.plugin.PluginLoader;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

@ParametrizedController("PluginPane.fxml")
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

  private final List<URI> jarUris = new ArrayList<>();
  private static final byte[] EMPTY_LIST_BYTES = new byte[0];

  @FXML
  private void initialize() {
    splitPane.setDividerPositions(0.75);
    pluginTable.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
      if (e.getCode() == KeyCode.ESCAPE) {
        root.getScene().getWindow().hide();
      }
    });
    nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().idString()));
    versionColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getVersion().toString()));
    loadedColumn.setCellValueFactory(data -> {
      Plugin plugin = data.getValue();
      BooleanProperty prop = new SimpleBooleanProperty(plugin.isLoaded());
      PluginLoader.getDefault().getLoadedPlugins().addListener((SetChangeListener<Plugin>) change -> {
        if (change.wasRemoved()) {
          Plugin unloaded = change.getElementRemoved();
          if (unloaded.equals(plugin)) {
            prop.setValue(false);
          }
        }
      });
      prop.addListener((__, was, is) -> {
        if (was) {
          PluginLoader.getDefault().unload(plugin);
        } else {
          PluginLoader.getDefault().load(plugin);
        }
        // Force update the checkboxes to make sure they're disabled when their plugin can't be loaded
        pluginTable.lookupAll(".check-box-table-cell").stream()
            .flatMap(TypeUtils.castStream(CheckBoxTableCell.class))
            .filter(c -> c.getTableColumn() == loadedColumn)
            .filter(c -> !c.isEmpty())
            .forEach(c -> c.updateItem(c.getItem(), c.isEmpty()));
      });
      return prop;
    });
    //TODO use toggle switches?
    loadedColumn.setCellFactory(column -> new CheckBoxTableCell<Plugin, Boolean>() {

      @Override
      public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        int index = getIndex();
        ObservableList<Plugin> items = getTableView().getItems();
        if (!empty && index >= 0 && index < items.size()) {
          Plugin plugin = items.get(index);
          setEditable(PluginLoader.getDefault().canLoad(plugin));
        }
      }

    });
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
        + "\n\n" + plugin.getSummary();
  }

  @FXML
  private void loadPlugin() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choose a plugin");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Plugin", "*.jar"));
    List<File> files = chooser.showOpenMultipleDialog(root.getScene().getWindow());
    if (files == null) {
      return;
    }
    files.forEach(f -> {
      try {
        PluginLoader.getDefault().loadPluginJar(f.toURI());
        jarUris.add(f.toURI());
      } catch (IOException | IllegalArgumentException e) {
        log.log(Level.WARNING, "Could not load jar", e);
        // TODO improve the dialog; use something like what GRIP has
        Alert alert = new Alert(Alert.AlertType.ERROR, null, ButtonType.OK);
        alert.setTitle("Could not load plugins");
        alert.setHeaderText("Plugins in " + f.getName() + " could not be loaded");
        alert.setContentText("Error message:\n\n    " + e.getMessage());
        alert.showAndWait();
      }
    });
    PluginCache.getDefault().saveToCache(jarUris);
  }

  @FXML
  private void clearCache() {
    try {
      log.info("Clearing plugin cache");
      jarUris.clear();
      Files.write(Storage.getPluginCache(), EMPTY_LIST_BYTES);
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not clear cache", e);
    }
  }

}
