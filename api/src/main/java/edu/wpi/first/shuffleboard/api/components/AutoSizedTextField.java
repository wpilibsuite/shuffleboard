package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.util.TextUtils;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import org.fxmisc.easybind.EasyBind;

/** A TextField that resizes itself to fit its content. */
public class AutoSizedTextField extends TextField {

  /** Default constructor. */
  public AutoSizedTextField() {
    setPadding(new Insets(3));
    prefWidthProperty()
        .bind(
            EasyBind.combine(
                visibleProperty(),
                textProperty(),
                fontProperty(),
                (visible, text, font) -> {
                  return visible ? TextUtils.computeTextWidth(font, text) + 7 : 1;
                }));
  }
}
