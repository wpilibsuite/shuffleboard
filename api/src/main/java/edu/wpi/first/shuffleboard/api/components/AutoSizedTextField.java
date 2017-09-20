package edu.wpi.first.shuffleboard.api.components;

import com.sun.javafx.tk.Toolkit;

import org.fxmisc.easybind.EasyBind;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;

/**
 * A TextField that resizes itself to fit its content.
 */
public class AutoSizedTextField extends TextField {

  /**
   * Default constructor.
   */
  public AutoSizedTextField() {
    setPadding(new Insets(3));
    prefWidthProperty().bind(
        EasyBind.combine(visibleProperty(), textProperty(), fontProperty(), (visible, text, font) -> {
          double width = Toolkit.getToolkit().getFontLoader().computeStringWidth(text, font);
          return visible ? width + 7 : 1;
        })
    );
  }
}
