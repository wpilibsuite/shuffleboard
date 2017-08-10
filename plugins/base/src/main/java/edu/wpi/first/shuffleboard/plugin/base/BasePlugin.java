package edu.wpi.first.shuffleboard.plugin.base;

import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.SimpleAdapter;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BooleanArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BooleanType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.EncoderType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.MapType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.NumberType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RawByteType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SendableChooserType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SpeedControllerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringType;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.BooleanArrayAdapter;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.NumberArrayAdapter;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.StringAdapter;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.StringArrayAdapter;
import edu.wpi.first.shuffleboard.plugin.base.widget.BooleanBox;
import edu.wpi.first.shuffleboard.plugin.base.widget.ComboBoxChooser;
import edu.wpi.first.shuffleboard.plugin.base.widget.EncoderWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.NumberSlider;
import edu.wpi.first.shuffleboard.plugin.base.widget.ProgressBar;
import edu.wpi.first.shuffleboard.plugin.base.widget.SpeedController;
import edu.wpi.first.shuffleboard.plugin.base.widget.TextView;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleButton;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleSwitch;
import edu.wpi.first.shuffleboard.plugin.base.widget.VoltageViewWidget;

import java.util.List;

public class BasePlugin extends Plugin {

  public BasePlugin() {
    super("Base");
  }

  @Override
  public List<DataType> getDataTypes() {
    return ImmutableList.of(
        new BooleanType(),
        new NumberType(),
        new StringType(),
        new BooleanArrayType(),
        new NumberArrayType(),
        new StringArrayType(),
        new RawByteType(),
        new MapType(),
        new AnalogInputType(),
        new EncoderType(),
        new SendableChooserType(),
        new SpeedControllerType()
    );
  }

  @Override
  public List<Class<? extends Widget>> getWidgets() {
    return ImmutableList.of(
        BooleanBox.class,
        ComboBoxChooser.class,
        EncoderWidget.class,
        NumberSlider.class,
        ProgressBar.class,
        SpeedController.class,
        TextView.class,
        ToggleButton.class,
        ToggleSwitch.class,
        VoltageViewWidget.class
    );
  }

  @Override
  public List<TypeAdapter> getTypeAdapters() {
    return ImmutableList.of(
        new SimpleAdapter<>(new NumberType(),
            n -> Serialization.toByteArray(n.doubleValue()), Serialization::readDouble, Serialization.SIZE_OF_DOUBLE),
        new SimpleAdapter<>(new BooleanType(),
            Serialization::toByteArray, Serialization::readBoolean, Serialization.SIZE_OF_BOOL),
        new StringAdapter(),
        new NumberArrayAdapter(),
        new BooleanArrayAdapter(),
        new StringArrayAdapter()
    );
  }

}
