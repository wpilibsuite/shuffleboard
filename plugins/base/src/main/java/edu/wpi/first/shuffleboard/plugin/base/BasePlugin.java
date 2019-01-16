package edu.wpi.first.shuffleboard.plugin.base;

import edu.wpi.first.shuffleboard.api.PropertyParser;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.json.ElementTypeAdapter;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.LayoutClass;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AccelerometerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BasicSubsystemType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.CommandType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.DifferentialDriveType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.EncoderType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.FmsInfoType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.GyroType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.MecanumDriveType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.PIDCommandType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.PIDControllerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.PowerDistributionType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.QuadratureEncoderType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RelayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RobotPreferencesType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SendableChooserType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SpeedControllerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SubsystemType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.ThreeAxisAccelerometerType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.UltrasonicType;
import edu.wpi.first.shuffleboard.plugin.base.layout.GridLayout;
import edu.wpi.first.shuffleboard.plugin.base.layout.GridLayoutSaver;
import edu.wpi.first.shuffleboard.plugin.base.layout.ListLayout;
import edu.wpi.first.shuffleboard.plugin.base.layout.SubsystemLayout;
import edu.wpi.first.shuffleboard.plugin.base.widget.AccelerometerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.BasicFmsInfoWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.BasicSubsystemWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.BooleanBoxWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ComboBoxChooserWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.CommandWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.DifferentialDriveWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.EncoderWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.GraphWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.GyroWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.MecanumDriveWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.NumberBarWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.NumberSliderWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.PIDCommandWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.PIDControllerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.PowerDistributionPanelWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.RelayWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.RobotPreferencesWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.SimpleDialWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.SpeedControllerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.SplitButtonChooserWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.TextViewWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ThreeAxisAccelerometerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleButtonWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleSwitchWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.UltrasonicWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.VoltageViewWidget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Description(
    group = "edu.wpi.first.shuffleboard",
    name = "Base",
    version = "1.1.4",
    summary = "Defines all the WPILib data types and stock widgets"
)
@SuppressWarnings("PMD.CouplingBetweenObjects")
public class BasePlugin extends Plugin {

  @Override
  public List<DataType> getDataTypes() {
    return ImmutableList.of(
        AnalogInputType.Instance,
        PowerDistributionType.Instance,
        EncoderType.Instance,
        QuadratureEncoderType.Instance,
        RobotPreferencesType.Instance,
        SendableChooserType.Instance,
        SpeedControllerType.Instance,
        SubsystemType.Instance,
        BasicSubsystemType.Instance,
        CommandType.Instance,
        PIDCommandType.Instance,
        PIDControllerType.Instance,
        AccelerometerType.Instance,
        ThreeAxisAccelerometerType.Instance,
        GyroType.Instance,
        RelayType.Instance,
        MecanumDriveType.Instance,
        DifferentialDriveType.Instance,
        FmsInfoType.Instance,
        UltrasonicType.Instance
    );
  }

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(BooleanBoxWidget.class),
        WidgetType.forAnnotatedWidget(ToggleButtonWidget.class),
        WidgetType.forAnnotatedWidget(ToggleSwitchWidget.class),
        WidgetType.forAnnotatedWidget(NumberSliderWidget.class),
        WidgetType.forAnnotatedWidget(NumberBarWidget.class),
        WidgetType.forAnnotatedWidget(SimpleDialWidget.class),
        WidgetType.forAnnotatedWidget(GraphWidget.class),
        WidgetType.forAnnotatedWidget(TextViewWidget.class),
        WidgetType.forAnnotatedWidget(VoltageViewWidget.class),
        WidgetType.forAnnotatedWidget(PowerDistributionPanelWidget.class),
        WidgetType.forAnnotatedWidget(ComboBoxChooserWidget.class),
        WidgetType.forAnnotatedWidget(SplitButtonChooserWidget.class),
        WidgetType.forAnnotatedWidget(EncoderWidget.class),
        WidgetType.forAnnotatedWidget(RobotPreferencesWidget.class),
        WidgetType.forAnnotatedWidget(SpeedControllerWidget.class),
        WidgetType.forAnnotatedWidget(CommandWidget.class),
        WidgetType.forAnnotatedWidget(BasicSubsystemWidget.class),
        WidgetType.forAnnotatedWidget(PIDCommandWidget.class),
        WidgetType.forAnnotatedWidget(AccelerometerWidget.class),
        WidgetType.forAnnotatedWidget(ThreeAxisAccelerometerWidget.class),
        WidgetType.forAnnotatedWidget(PIDControllerWidget.class),
        WidgetType.forAnnotatedWidget(GyroWidget.class),
        WidgetType.forAnnotatedWidget(RelayWidget.class),
        WidgetType.forAnnotatedWidget(DifferentialDriveWidget.class),
        WidgetType.forAnnotatedWidget(MecanumDriveWidget.class),
        WidgetType.forAnnotatedWidget(BasicFmsInfoWidget.class),
        WidgetType.forAnnotatedWidget(UltrasonicWidget.class),
        new LayoutClass<>("List Layout", ListLayout.class),
        new LayoutClass<>("Grid Layout", GridLayout.class),
        createSubsystemLayoutType()
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.<DataType, ComponentType>builder()
        .put(DataTypes.Boolean, WidgetType.forAnnotatedWidget(BooleanBoxWidget.class))
        .put(DataTypes.Number, WidgetType.forAnnotatedWidget(TextViewWidget.class))
        .put(DataTypes.String, WidgetType.forAnnotatedWidget(TextViewWidget.class))
        .put(AnalogInputType.Instance, WidgetType.forAnnotatedWidget(VoltageViewWidget.class))
        .put(PowerDistributionType.Instance, WidgetType.forAnnotatedWidget(PowerDistributionPanelWidget.class))
        .put(SendableChooserType.Instance, WidgetType.forAnnotatedWidget(ComboBoxChooserWidget.class))
        .put(EncoderType.Instance, WidgetType.forAnnotatedWidget(EncoderWidget.class))
        .put(QuadratureEncoderType.Instance, WidgetType.forAnnotatedWidget(EncoderWidget.class))
        .put(RobotPreferencesType.Instance, WidgetType.forAnnotatedWidget(RobotPreferencesWidget.class))
        .put(SpeedControllerType.Instance, WidgetType.forAnnotatedWidget(SpeedControllerWidget.class))
        .put(CommandType.Instance, WidgetType.forAnnotatedWidget(CommandWidget.class))
        .put(PIDCommandType.Instance, WidgetType.forAnnotatedWidget(PIDCommandWidget.class))
        .put(PIDControllerType.Instance, WidgetType.forAnnotatedWidget(PIDControllerWidget.class))
        .put(AccelerometerType.Instance, WidgetType.forAnnotatedWidget(AccelerometerWidget.class))
        .put(ThreeAxisAccelerometerType.Instance, WidgetType.forAnnotatedWidget(ThreeAxisAccelerometerWidget.class))
        .put(GyroType.Instance, WidgetType.forAnnotatedWidget(GyroWidget.class))
        .put(RelayType.Instance, WidgetType.forAnnotatedWidget(RelayWidget.class))
        .put(DifferentialDriveType.Instance, WidgetType.forAnnotatedWidget(DifferentialDriveWidget.class))
        .put(MecanumDriveType.Instance, WidgetType.forAnnotatedWidget(MecanumDriveWidget.class))
        .put(FmsInfoType.Instance, WidgetType.forAnnotatedWidget(BasicFmsInfoWidget.class))
        .put(UltrasonicType.Instance, WidgetType.forAnnotatedWidget(UltrasonicWidget.class))
        .put(BasicSubsystemType.Instance, WidgetType.forAnnotatedWidget(BasicSubsystemWidget.class))
        .put(SubsystemType.Instance, createSubsystemLayoutType())
        .build();
  }

  @Override
  public Set<PropertyParser<?>> getPropertyParsers() {
    return Set.of(
        PropertyParser.forEnum(ThreeAxisAccelerometerWidget.Range.class),
        PropertyParser.forEnum(UltrasonicWidget.Unit.class)
    );
  }

  private static LayoutClass<SubsystemLayout> createSubsystemLayoutType() {
    return new LayoutClass<SubsystemLayout>("Subsystem Layout", SubsystemLayout.class) {
      @Override
      public Set<DataType> getDataTypes() {
        return ImmutableSet.of(SubsystemType.Instance);
      }
    };
  }

  @Override
  public List<TabInfo> getDefaultTabInfo() {
    return ImmutableList.of(
        TabInfo.builder().name("SmartDashboard").autoPopulate().sourcePrefix("SmartDashboard/").build(),
        TabInfo.builder().name("LiveWindow").autoPopulate().sourcePrefix("LiveWindow/").build()
    );
  }

  @Override
  public List<ElementTypeAdapter<?>> getCustomTypeAdapters() {
    return ImmutableList.of(
        GridLayoutSaver.Instance
    );
  }

}
