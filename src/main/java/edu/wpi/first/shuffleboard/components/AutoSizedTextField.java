package edu.wpi.first.shuffleboard.components;

import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.TextField;
import org.fxmisc.easybind.EasyBind;

/**
 * A TextField that resizes itself to fit its content.
 */
public class AutoSizedTextField extends TextField {

  /**
   * Default constructor.
   */
  public AutoSizedTextField() {
    prefWidthProperty().bind(
        EasyBind.combine(visibleProperty(), textProperty(), fontProperty(), (visible, text, font) -> {
          double width = Toolkit.getToolkit().getFontLoader().computeStringWidth(text, font);
          return visible ? width + 20 : 1;
        })
    );
  }
}
