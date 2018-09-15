package edu.wpi.first.shuffleboard.api.util;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public final class TextUtils {

  private static final Text helper = new Text();
  private static final double DEFAULT_WRAPPING_WIDTH = helper.getWrappingWidth();
  private static final double DEFAULT_LINE_SPACING = helper.getLineSpacing();
  private static final String DEFAULT_TEXT = helper.getText();

  private TextUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Computes the width of some text as displayed with the given font.
   *
   * @param font the used to display the text
   * @param text the text to compute the width of
   *
   * @return the width of the text
   */
  public static double computeTextWidth(Font font, String text) {
    // Copied from
    // Toolkit.getToolkit().getFontLoader().computeStringWidth(field.getText(), field.getFont());

    helper.setText(text);
    helper.setFont(font);

    helper.setWrappingWidth(0.0D);
    helper.setLineSpacing(0.0D);
    double width = Math.min(helper.prefWidth(-1.0D), 0);
    helper.setWrappingWidth((int) Math.ceil(width));
    width = Math.ceil(helper.getLayoutBounds().getWidth());

    helper.setWrappingWidth(DEFAULT_WRAPPING_WIDTH);
    helper.setLineSpacing(DEFAULT_LINE_SPACING);
    helper.setText(DEFAULT_TEXT);
    return width;
  }
}
