package edu.wpi.first.shuffleboard.api.components;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import java.io.IOException;
import java.net.URL;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * A type of dialog that defaults to be undecorated and non-modal. It will automatically close if the user
 * presses the ESC key or (optionally) when it loses focus. A dialog closed in this manner will always have a result of
 * {@link ButtonType#CLOSE}.
 *
 * <p>This type of dialog also supports subheader text, which is usually shown in a slightly darker color and default
 * font size.
 */
public class ShuffleboardDialog extends Dialog<ButtonType> {

  private final BooleanProperty closeOnFocusLost = new SimpleBooleanProperty(this, "closeOnFocusLost", false);
  private final ChangeListener<Boolean> close = (__, was, is) -> {
    if (!is) {
      closeAndCancel();
    }
  };

  private final StringProperty subheaderText = new SimpleStringProperty(this, "subheaderText", null);

  private final MonadicBinding<Boolean> focus = EasyBind.monadic(dialogPaneProperty())
      .map(Node::getScene)
      .map(Scene::getWindow)
      .flatMap(Window::focusedProperty)
      .orElse(false);

  /**
   * Creates a new shuffleboard dialog with its content set to the contents of the given FXML file.
   *
   * @param fxmlLocation the location of the FXML to load and set the content to
   *
   * @throws IllegalArgumentException if the FXML could not be loaded
   */
  public static ShuffleboardDialog createForFxml(URL fxmlLocation) throws IllegalArgumentException {
    try {
      return new ShuffleboardDialog(FXMLLoader.load(fxmlLocation));
    } catch (IOException e) {
      throw new IllegalArgumentException("Unloadable FXML: " + fxmlLocation, e);
    }
  }

  /**
   * Creates a new dialog with the given content.
   *
   * @param content the content of the dialog
   */
  public ShuffleboardDialog(Node content) {
    initModality(Modality.APPLICATION_MODAL);
    initStyle(StageStyle.UNDECORATED);
    getDialogPane().setHeader(new Header());
    getDialogPane().setExpandableContent(null);
    getDialogPane().setContent(content);
    getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, this::closeIfEscapePressed);
    closeOnFocusLost.addListener((__, was, is) -> {
      if (is) {
        focus.addListener(close);
        initModality(Modality.NONE);
      } else {
        focus.removeListener(close);
        initModality(Modality.APPLICATION_MODAL);
      }
    });
  }

  public ShuffleboardDialog(Node content, boolean closeOnFocusLost) {
    this(content);
    setCloseOnFocusLost(closeOnFocusLost);
  }

  public final boolean isCloseOnFocusLost() {
    return closeOnFocusLost.get();
  }

  public final BooleanProperty closeOnFocusLostProperty() {
    return closeOnFocusLost;
  }

  public final void setCloseOnFocusLost(boolean closeOnFocusLost) {
    this.closeOnFocusLost.set(closeOnFocusLost);
  }

  private void closeIfEscapePressed(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      closeAndCancel();
    }
  }

  /**
   * Closes this dialog and sets the result to {@link ButtonType#CLOSE}.
   */
  public void closeAndCancel() {
    // Need to check this to avoid macOS's weird window handling behavior, which will trigger a focusLost event when
    // the dialog closes due to a user selection of a button in the dialog.
    // Linux and Windows don't seem to have this problem.
    if (isShowing()) {
      setResult(ButtonType.CLOSE);
      close();
    }
  }

  public final String getSubheaderText() {
    return subheaderText.get();
  }

  public final StringProperty subheaderTextProperty() {
    return subheaderText;
  }

  public final void setSubheaderText(String subheaderText) {
    this.subheaderText.set(subheaderText);
  }

  private final class Header extends VBox {

    private final Label title = new Label();
    private final Label subtitle = new Label();

    private final ChangeListener<String> removeIfNullText = (property, oldText, newText) -> {
      Node bean = (Node) ((Property) property).getBean();
      if (newText == null) {
        getChildren().remove(bean);
      } else {
        getChildren().add(bean);
      }
    };

    public Header() {
      setMaxWidth(Region.USE_COMPUTED_SIZE);
      title.textProperty().addListener(removeIfNullText);
      subtitle.textProperty().addListener(removeIfNullText);

      title.textProperty().bind(headerTextProperty());
      subtitle.textProperty().bind(subheaderTextProperty());

      title.getStyleClass().add("shuffleboard-dialog-header-title");
      subtitle.getStyleClass().add("shuffleboard-dialog-header-subtitle");
      getStyleClass().addAll("header-panel", "shuffleboard-dialog-header");
    }

  }


}
