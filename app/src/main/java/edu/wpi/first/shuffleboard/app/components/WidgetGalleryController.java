package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.dnd.DataFormats;

import java.io.IOException;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class WidgetGalleryController {
  @FXML
  private WidgetGallery root;

  @FXML
  private void initialize() throws IOException {
    root.getChildren().addListener((ListChangeListener<? super Node>) change -> {
      while (change.next()) {
        for (Node node : change.getAddedSubList()) {
          if (node instanceof WidgetGallery.WidgetGalleryItem) {
            WidgetGallery.WidgetGalleryItem galleryItem = (WidgetGallery.WidgetGalleryItem) node;
            galleryItem.setOnDragDetected(event -> {
              Dragboard dragboard = galleryItem.startDragAndDrop(TransferMode.COPY);

              // TODO type safety
              ClipboardContent clipboard = new ClipboardContent();
              clipboard.put(DataFormats.widgetType, galleryItem.getWidget().getName());
              dragboard.setContent(clipboard);
              event.consume();
            });
          }
        }
      }
    });
  }
}
