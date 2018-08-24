package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.css.SimpleColorCssMetaData;
import edu.wpi.first.shuffleboard.api.css.SimpleCssMetaData;

import com.google.common.collect.ImmutableList;

import eu.hansolo.medusa.Gauge;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.converter.EnumConverter;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 * A subclass of {@link Gauge} that allows various UI properties to be styled with CSS. Gauges are able to be styled
 * on a per-skin basis with the pseudoclass for that skins name; for example, a gauge with the {@code FLAT} skin can
 * be styled with {@code .gauge:flat} without affecting other gauge types.
 */
public class StyleableGauge extends Gauge {

  private static final Map<SkinType, PseudoClass> skinTypePseudoClasses = new EnumMap<>(SkinType.class);

  static {
    for (SkinType skinType : SkinType.values()) {
      String name = skinType.name().replace('_', '-').toLowerCase(Locale.US);
      skinTypePseudoClasses.put(skinType, PseudoClass.getPseudoClass(name));
    }
  }

  public StyleableGauge() {
    super();
  }

  public StyleableGauge(SkinType skinType) {
    super(skinType);
  }

  @Override
  protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
    return GaugeCss.STYLEABLES;
  }

  @Override
  public String getUserAgentStylesheet() {
    // Properly implement this method to get the base stylesheet
    // It's empty (as of Medusa 7.9) but this lets us use any changes made to it in the future
    return Gauge.class.getResource("gauge.css").toExternalForm();
  }

  @Override
  public void setSkinType(SkinType skinType) {
    SkinType previousSkin = getSkinType();
    if (skinType != previousSkin) {
      super.setSkinType(skinType);
      pseudoClassStateChanged(skinTypePseudoClasses.get(previousSkin), false);
      pseudoClassStateChanged(skinTypePseudoClasses.get(skinType), true);
    }
  }

  /**
   * A helper class that holds all the styleable properties.
   */
  private static final class GaugeCss {

    private static final CssMetaData<Gauge, Paint> BACKGROUND_PAINT =
        new SimpleCssMetaData<>(
            "-fx-background-color",
            StyleConverter.getPaintConverter(),
            Gauge::backgroundPaintProperty
        );
    private static final CssMetaData<Gauge, Color> BAR_BACKGROUND_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-bar-background-color",
            Gauge::barBackgroundColorProperty
        );
    private static final CssMetaData<Gauge, Color> BAR_BORDER_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-bar-border-color",
            Gauge::barBorderColorProperty
        );
    private static final CssMetaData<Gauge, Color> BAR_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-bar-color",
            Gauge::barColorProperty
        );
    private static final CssMetaData<Gauge, Paint> BORDER_PAINT =
        new SimpleCssMetaData<>(
            "-fx-border-color",
            StyleConverter.getPaintConverter(),
            Gauge::borderPaintProperty
        );
    private static final CssMetaData<Gauge, Number> BORDER_WIDTH =
        new SimpleCssMetaData<>(
            "-fx-border-width",
            StyleConverter.getSizeConverter(),
            Gauge::borderWidthProperty
        );
    private static final CssMetaData<Gauge, Font> CUSTOM_FONT =
        new SimpleCssMetaData<>(
            "-fx-custom-font",
            StyleConverter.getFontConverter(),
            Gauge::customFontProperty
        );
    private static final CssMetaData<Gauge, Boolean> CUSTOM_FONT_ENABLED =
        new SimpleCssMetaData<>(
            "-fx-custom-font-enabled",
            StyleConverter.getBooleanConverter(),
            Gauge::customFontEnabledProperty
        );
    private static final CssMetaData<Gauge, Paint> FOREGROUND_PAINT =
        new SimpleCssMetaData<>(
            "-fx-foreground-color",
            StyleConverter.getPaintConverter(),
            Gauge::foregroundPaintProperty
        );
    private static final CssMetaData<Gauge, Boolean> INNER_SHADOW_ENABLED =
        new SimpleCssMetaData<>(
            "-fx-inner-shadow-enabled",
            StyleConverter.getBooleanConverter(),
            Gauge::innerShadowEnabledProperty
        );
    private static final CssMetaData<Gauge, Boolean> KEEP_ASPECT_RATIO =
        new SimpleCssMetaData<>(
            "-fx-keep-aspect-ratio",
            StyleConverter.getBooleanConverter(),
            Gauge::keepAspectProperty
        );
    private static final CssMetaData<Gauge, Color> KNOB_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-knob-color",
            Gauge::knobColorProperty
        );
    private static final CssMetaData<Gauge, KnobType> KNOB_TYPE =
        new SimpleCssMetaData<>(
            "-fx-knob-type",
            new EnumConverter<>(KnobType.class),
            Gauge::knobTypeProperty
        );
    private static final CssMetaData<Gauge, Boolean> KNOB_VISIBLE =
        new SimpleCssMetaData<>(
            "-fx-knob-visible",
            StyleConverter.getBooleanConverter(),
            Gauge::knobVisibleProperty
        );
    private static final CssMetaData<Gauge, Color> MAJOR_TICK_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-major-tick-color",
            Gauge::majorTickMarkColorProperty
        );
    private static final CssMetaData<Gauge, Color> MEDIUM_TICK_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-medium-tick-color",
            Gauge::mediumTickMarkColorProperty
        );
    private static final CssMetaData<Gauge, Color> MINOR_TICK_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-minor-tick-color",
            Gauge::minorTickMarkColorProperty
        );
    private static final CssMetaData<Gauge, Color> NEEDLE_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-needle-color",
            Gauge::needleColorProperty
        );
    private static final CssMetaData<Gauge, NeedleShape> NEEDLE_SHAPE =
        new SimpleCssMetaData<>(
            "-fx-needle-shape",
            new EnumConverter<>(NeedleShape.class),
            Gauge::needleShapeProperty
        );
    private static final CssMetaData<Gauge, NeedleSize> NEEDLE_SIZE =
        new SimpleCssMetaData<>(
            "-fx-needle-size",
            new EnumConverter<>(NeedleSize.class),
            Gauge::needleSizeProperty
        );
    private static final CssMetaData<Gauge, NeedleType> NEEDLE_TYPE =
        new SimpleCssMetaData<>(
            "-fx-needle-type",
            new EnumConverter<>(NeedleType.class),
            Gauge::needleTypeProperty
        );
    private static final CssMetaData<Gauge, Number> START_ANGLE =
        new SimpleCssMetaData<>(
            "-fx-start-angle",
            StyleConverter.getSizeConverter(),
            Gauge::startAngleProperty
        );
    private static final CssMetaData<Gauge, Color> SUBTITLE_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-subtitle-color",
            Gauge::subTitleColorProperty
        );
    private static final CssMetaData<Gauge, Color> TICK_LABEL_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-tick-label-color",
            Gauge::tickLabelColorProperty
        );
    private static final CssMetaData<Gauge, Color> TICK_MARK_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-tick-mark-color",
            Gauge::tickMarkColorProperty
        );
    private static final CssMetaData<Gauge, Boolean> TICK_MARK_RING_VISIBLE =
        new SimpleCssMetaData<>(
            "-fx-tick-mark-ring-visible",
            StyleConverter.getBooleanConverter(),
            Gauge::tickMarkRingVisibleProperty
        );
    private static final CssMetaData<Gauge, Color> TITLE_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-title-color",
            Gauge::titleColorProperty
        );
    private static final CssMetaData<Gauge, Color> UNIT_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-unit-color",
            Gauge::unitColorProperty
        );
    private static final CssMetaData<Gauge, Color> VALUE_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-value-color",
            Gauge::valueColorProperty
        );
    private static final CssMetaData<Gauge, Color> ZERO_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-zero-color",
            Gauge::zeroColorProperty
        );

    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = ImmutableList.of(
        BACKGROUND_PAINT,
        BAR_BACKGROUND_COLOR,
        BAR_BORDER_COLOR,
        BAR_COLOR,
        BORDER_PAINT,
        BORDER_WIDTH,
        CUSTOM_FONT,
        CUSTOM_FONT_ENABLED,
        FOREGROUND_PAINT,
        INNER_SHADOW_ENABLED,
        KEEP_ASPECT_RATIO,
        KNOB_COLOR,
        KNOB_TYPE,
        KNOB_VISIBLE,
        MAJOR_TICK_COLOR,
        MEDIUM_TICK_COLOR,
        MINOR_TICK_COLOR,
        NEEDLE_COLOR,
        NEEDLE_SHAPE,
        NEEDLE_SIZE,
        NEEDLE_TYPE,
        START_ANGLE,
        SUBTITLE_COLOR,
        TICK_LABEL_COLOR,
        TICK_MARK_COLOR,
        TICK_MARK_RING_VISIBLE,
        TITLE_COLOR,
        UNIT_COLOR,
        VALUE_COLOR,
        ZERO_COLOR
    );

  }

}
