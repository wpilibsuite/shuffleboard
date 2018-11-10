package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.sources.recording.ConversionSettings;
import edu.wpi.first.shuffleboard.api.sources.recording.Converter;
import edu.wpi.first.shuffleboard.api.sources.recording.Converters;
import edu.wpi.first.shuffleboard.api.sources.recording.Recording;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.PreferencesUtils;
import edu.wpi.first.shuffleboard.api.util.Storage;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import org.controlsfx.control.ToggleSwitch;
import org.fxmisc.easybind.EasyBind;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

@ParametrizedController("ConvertRecordingPane.fxml")
public final class ConvertRecordingPaneController {

  private static final Logger log = Logger.getLogger(ConvertRecordingPaneController.class.getName());

  @FXML
  private Pane root;
  @FXML
  private ListView<File> fileList;
  @FXML
  private ComboBox<Converter> formatDropdown;
  @FXML
  private ToggleSwitch includeMetadata;
  @FXML
  private TextField destinationDirField;
  @FXML
  private Label changeDestinationLabel;
  @FXML
  private Button convertButton;

  private final ExecutorService conversionExecutor = Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread);

  private final Preferences prefs = Preferences.userNodeForPackage(ConvertRecordingPaneController.class);

  private final Property<File> sourceFileDir = new SimpleObjectProperty<>(this, "sourceFileDir", null);
  private final Property<File> outputDir = new SimpleObjectProperty<>(this, "outputDir", null);
  private final ObservableList<File> sourceFiles = FXCollections.observableArrayList();
  private final BooleanProperty exportable = new SimpleBooleanProperty(this, "exportable", false);

  @FXML
  private void initialize() throws IOException {
    try {
      sourceFileDir.setValue(Storage.getRecordingDir());
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not get recordings dir", e);
    }
    PreferencesUtils.read(sourceFileDir, prefs, File::new);
    PreferencesUtils.read(outputDir, prefs, File::new);
    if (outputDir.getValue() == null) {
      outputDir.setValue(Storage.getRecordingDir());
    }
    sourceFileDir.addListener(__ -> PreferencesUtils.save(sourceFileDir, prefs, File::getAbsolutePath));
    outputDir.addListener(__ -> PreferencesUtils.save(outputDir, prefs, File::getAbsolutePath));

    exportable.bind(Bindings.createBooleanBinding(this::calcExportability, sourceFiles, outputDir));

    fileList.setItems(sourceFiles);

    fileList.setCellFactory(view -> {
      ListCell<File> cell = new ListCell<>();
      cell.textProperty().bind(EasyBind.monadic(cell.itemProperty()).map(File::getName));
      return cell;
    });

    formatDropdown.setConverter(new StringConverter<Converter>() {
      @Override
      public String toString(Converter converter) {
        return converter.formatName();
      }

      @Override
      public Converter fromString(String formatName) {
        return Converters.getDefault().getItems()
            .stream()
            .filter(e -> e.formatName().equals(formatName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("No converter for format '" + formatName + "'"));
      }
    });
    EasyBind.listBind(formatDropdown.getItems(), Converters.getDefault().getItems());
    formatDropdown.getItems().sort(Comparator.comparing(Converter::formatName));
    formatDropdown.getSelectionModel().select(0);
    destinationDirField.textProperty().bind(EasyBind.monadic(outputDir).map(File::getPath));
  }

  private boolean calcExportability() {
    return !sourceFiles.isEmpty() && outputDir.getValue() != null;
  }

  @FXML
  private void addFiles() throws IOException {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choose recording files");
    chooser.getExtensionFilters().setAll(
        new FileChooser.ExtensionFilter("Shuffleboard Data Recording", "*.sbr"));
    chooser.setInitialDirectory(
        EasyBind.monadic(sourceFileDir)
            .orElse(Storage.getRecordingDir())
            .get());
    List<File> selected = chooser.showOpenMultipleDialog(null);
    if (selected == null || selected.isEmpty()) {
      return;
    }
    long numDirs = selected.stream()
        .map(File::getParentFile)
        .distinct()
        .count();
    if (numDirs == 1) {
      sourceFileDir.setValue(selected.get(0).getParentFile());
    }
    selected.forEach(this::addIfNotPresent);
  }

  @FXML
  private void addDirectory() throws IOException {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Choose directory");
    chooser.setInitialDirectory(
        EasyBind.monadic(sourceFileDir)
            .orElse(Storage.getRecordingDir())
            .get());
    File dir = chooser.showDialog(null);
    if (dir == null) {
      return;
    }
    Files.list(dir.toPath())
        .filter(p -> p.toString().endsWith(".sbr"))
        .map(Path::toFile)
        .forEach(this::addIfNotPresent);
  }

  private void addIfNotPresent(File file) {
    if (!sourceFiles.contains(file)) {
      sourceFiles.add(file);
    }
  }

  @FXML
  private void chooseDestinationDir() throws IOException {
    DirectoryChooser chooser = new DirectoryChooser();
    chooser.setTitle("Choose output folder");
    File initialDir = outputDir.getValue();
    if (initialDir.isDirectory()) {
      chooser.setInitialDirectory(initialDir);
    } else {
      chooser.setInitialDirectory(Storage.getRecordingDir());
    }
    File dir = chooser.showDialog(null);
    if (dir != null) {
      outputDir.setValue(dir);
    }
  }

  @FXML
  private void convert() {
    Converter converter = formatDropdown.getSelectionModel().getSelectedItem();
    ConversionSettings settings = new ConversionSettings(includeMetadata.isSelected());
    conversionExecutor.submit(() -> {
      for (File file : sourceFiles) {
        try {
          Recording recording = Serialization.loadRecording(file.toPath());
          String dstFileName = file.getName().replace(".sbr", converter.fileExtension());
          Path dst = Paths.get(outputDir.getValue().getAbsolutePath(), dstFileName);
          log.info("Exporting " + file + " to " + dst);
          converter.export(recording, dst, settings);
        } catch (IOException | RuntimeException e) {
          log.log(Level.WARNING,
              "Could not export recording file " + file + " with converter " + converter.formatName(), e);
        }
      }
      FxUtils.runOnFxThread(sourceFiles::clear);
    });
  }

  public boolean isExportable() {
    return exportable.get();
  }

  public ReadOnlyBooleanProperty exportableProperty() {
    return exportable;
  }

}
