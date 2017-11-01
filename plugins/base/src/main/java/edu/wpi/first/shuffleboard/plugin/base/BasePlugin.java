package edu.wpi.first.shuffleboard.plugin.base;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.SimpleAdapter;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.LayoutClass;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BooleanArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BooleanType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.CommandType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.EncoderType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.GyroType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.NumberType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.PIDControllerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.PowerDistributionType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RawByteType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RelayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SendableChooserType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SpeedControllerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SubsystemType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.ThreeAxisAccelerometerType;
import edu.wpi.first.shuffleboard.plugin.base.layout.ListLayout;
import edu.wpi.first.shuffleboard.plugin.base.layout.SubsystemLayout;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.BooleanArrayAdapter;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.NumberArrayAdapter;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.StringAdapter;
import edu.wpi.first.shuffleboard.plugin.base.recording.serialization.StringArrayAdapter;
import edu.wpi.first.shuffleboard.plugin.base.widget.BooleanBox;
import edu.wpi.first.shuffleboard.plugin.base.widget.ComboBoxChooser;
import edu.wpi.first.shuffleboard.plugin.base.widget.CommandWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.EncoderWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.GyroWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.NumberBarWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.NumberSlider;
import edu.wpi.first.shuffleboard.plugin.base.widget.PIDControllerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.PowerDistributionPanelWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.RelayWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.SimpleDialWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.SpeedController;
import edu.wpi.first.shuffleboard.plugin.base.widget.TextView;
import edu.wpi.first.shuffleboard.plugin.base.widget.ThreeAxisAccelerometerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleButton;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleSwitch;
import edu.wpi.first.shuffleboard.plugin.base.widget.VoltageViewWidget;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BasePlugin extends Plugin {

  public BasePlugin() {
    super("edu.wpi.first.shuffleboard", "Base", "1.0.0",
        "Defines all the WPILib data types and stock widgets");
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
        new AnalogInputType(),
        new PowerDistributionType(),
        new EncoderType(),
        new SendableChooserType(),
        new SpeedControllerType(),
        new SubsystemType(),
        new CommandType(),
        new PIDControllerType(),
        new ThreeAxisAccelerometerType(),
        new GyroType(),
        new RelayType()
    );
  }

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(BooleanBox.class),
        WidgetType.forAnnotatedWidget(ToggleButton.class),
        WidgetType.forAnnotatedWidget(ToggleSwitch.class),
        WidgetType.forAnnotatedWidget(NumberSlider.class),
        WidgetType.forAnnotatedWidget(NumberBarWidget.class),
        WidgetType.forAnnotatedWidget(SimpleDialWidget.class),
        WidgetType.forAnnotatedWidget(TextView.class),
        WidgetType.forAnnotatedWidget(VoltageViewWidget.class),
        WidgetType.forAnnotatedWidget(PowerDistributionPanelWidget.class),
        WidgetType.forAnnotatedWidget(ComboBoxChooser.class),
        WidgetType.forAnnotatedWidget(EncoderWidget.class),
        WidgetType.forAnnotatedWidget(SpeedController.class),
        WidgetType.forAnnotatedWidget(CommandWidget.class),
        WidgetType.forAnnotatedWidget(ThreeAxisAccelerometerWidget.class),
        WidgetType.forAnnotatedWidget(PIDControllerWidget.class),
        WidgetType.forAnnotatedWidget(GyroWidget.class),
        WidgetType.forAnnotatedWidget(RelayWidget.class),
        new LayoutClass<>("List Layout", ListLayout.class),
        createSubsystemLayoutType()
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

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.<DataType, ComponentType>builder()
        .put(new BooleanType(), WidgetType.forAnnotatedWidget(BooleanBox.class))
        .put(new NumberType(), WidgetType.forAnnotatedWidget(TextView.class))
        .put(new StringType(), WidgetType.forAnnotatedWidget(TextView.class))
        .put(new AnalogInputType(), WidgetType.forAnnotatedWidget(VoltageViewWidget.class))
        .put(new PowerDistributionType(), WidgetType.forAnnotatedWidget(PowerDistributionPanelWidget.class))
        .put(new SendableChooserType(), WidgetType.forAnnotatedWidget(ComboBoxChooser.class))
        .put(new EncoderType(), WidgetType.forAnnotatedWidget(EncoderWidget.class))
        .put(new SpeedControllerType(), WidgetType.forAnnotatedWidget(SpeedController.class))
        .put(new CommandType(), WidgetType.forAnnotatedWidget(CommandWidget.class))
        .put(new ThreeAxisAccelerometerType(), WidgetType.forAnnotatedWidget(ThreeAxisAccelerometerWidget.class))
        .put(new PIDControllerType(), WidgetType.forAnnotatedWidget(PIDControllerWidget.class))
        .put(new GyroType(), WidgetType.forAnnotatedWidget(GyroWidget.class))
        .put(new RelayType(), WidgetType.forAnnotatedWidget(RelayWidget.class))
        .put(new SubsystemType(), createSubsystemLayoutType())
        .build();
  }

  private static LayoutClass<SubsystemLayout> createSubsystemLayoutType() {
    return new LayoutClass<SubsystemLayout>("Subsystem Layout", SubsystemLayout.class) {
      @Override
      public Set<DataType> getDataTypes() {
        return ImmutableSet.of(new SubsystemType());
      }
    };
  }

}
