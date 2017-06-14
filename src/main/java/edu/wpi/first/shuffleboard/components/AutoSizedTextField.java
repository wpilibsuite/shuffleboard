package edu.wpi.first.shuffleboard.components;

import com.sun.javafx.tk.Toolkit;
import javafx.beans.binding.Bindings;
import javafx.scene.control.TextField;

public class AutoSizedTextField extends TextField {
  /**
   * A TextField that resizes itself to fit it's content.
   */
  public AutoSizedTextField() {
    prefWidthProperty().bind(
        Bindings.createDoubleBinding(
          () -> {
            double width = Toolkit.getToolkit().getFontLoader().computeStringWidth(getText(), getFont());
            return isVisible() ? width + 20 : 1;
          },
          visibleProperty(), textProperty(), fontProperty())
    );
  }
}
