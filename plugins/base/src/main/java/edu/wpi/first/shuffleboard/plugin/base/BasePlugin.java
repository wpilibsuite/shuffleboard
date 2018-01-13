package edu.wpi.first.shuffleboard.plugin.base;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.LayoutClass;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.BasicSubsystemType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.CommandType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.DifferentialDriveType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.EncoderType;
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
import edu.wpi.first.shuffleboard.plugin.base.layout.ListLayout;
import edu.wpi.first.shuffleboard.plugin.base.layout.SubsystemLayout;
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
import edu.wpi.first.shuffleboard.plugin.base.widget.TextViewWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ThreeAxisAccelerometerWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleButtonWidget;
import edu.wpi.first.shuffleboard.plugin.base.widget.ToggleSwitchWidget;
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
    version = "1.0.0",
    summary = "Defines all the WPILib data types and stock widgets"
)
public class BasePlugin extends Plugin {

  @Override
  public List<DataType> getDataTypes() {
    return ImmutableList.of(
        new AnalogInputType(),
        new PowerDistributionType(),
        new EncoderType(),
        QuadratureEncoderType.Instance,
        new RobotPreferencesType(),
        new SendableChooserType(),
        new SpeedControllerType(),
        new SubsystemType(),
        BasicSubsystemType.Instance,
        new CommandType(),
        new PIDControllerType(),
        new ThreeAxisAccelerometerType(),
        new PIDCommandType(),
        new GyroType(),
        new RelayType(),
        MecanumDriveType.Instance,
        DifferentialDriveType.Instance
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
        WidgetType.forAnnotatedWidget(EncoderWidget.class),
        WidgetType.forAnnotatedWidget(RobotPreferencesWidget.class),
        WidgetType.forAnnotatedWidget(SpeedControllerWidget.class),
        WidgetType.forAnnotatedWidget(CommandWidget.class),
        WidgetType.forAnnotatedWidget(BasicSubsystemWidget.class),
        WidgetType.forAnnotatedWidget(PIDCommandWidget.class),
        WidgetType.forAnnotatedWidget(ThreeAxisAccelerometerWidget.class),
        WidgetType.forAnnotatedWidget(PIDControllerWidget.class),
        WidgetType.forAnnotatedWidget(GyroWidget.class),
        WidgetType.forAnnotatedWidget(RelayWidget.class),
        WidgetType.forAnnotatedWidget(DifferentialDriveWidget.class),
        WidgetType.forAnnotatedWidget(MecanumDriveWidget.class),
        new LayoutClass<>("List Layout", ListLayout.class),
        createSubsystemLayoutType()
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.<DataType, ComponentType>builder()
        .put(DataTypes.Boolean, WidgetType.forAnnotatedWidget(BooleanBoxWidget.class))
        .put(DataTypes.Number, WidgetType.forAnnotatedWidget(TextViewWidget.class))
        .put(DataTypes.String, WidgetType.forAnnotatedWidget(TextViewWidget.class))
        .put(new AnalogInputType(), WidgetType.forAnnotatedWidget(VoltageViewWidget.class))
        .put(new PowerDistributionType(), WidgetType.forAnnotatedWidget(PowerDistributionPanelWidget.class))
        .put(new SendableChooserType(), WidgetType.forAnnotatedWidget(ComboBoxChooserWidget.class))
        .put(new EncoderType(), WidgetType.forAnnotatedWidget(EncoderWidget.class))
        .put(QuadratureEncoderType.Instance, WidgetType.forAnnotatedWidget(EncoderWidget.class))
        .put(new RobotPreferencesType(), WidgetType.forAnnotatedWidget(RobotPreferencesWidget.class))
        .put(new SpeedControllerType(), WidgetType.forAnnotatedWidget(SpeedControllerWidget.class))
        .put(new CommandType(), WidgetType.forAnnotatedWidget(CommandWidget.class))
        .put(new PIDCommandType(), WidgetType.forAnnotatedWidget(PIDCommandWidget.class))
        .put(new ThreeAxisAccelerometerType(), WidgetType.forAnnotatedWidget(ThreeAxisAccelerometerWidget.class))
        .put(new PIDControllerType(), WidgetType.forAnnotatedWidget(PIDControllerWidget.class))
        .put(new GyroType(), WidgetType.forAnnotatedWidget(GyroWidget.class))
        .put(new RelayType(), WidgetType.forAnnotatedWidget(RelayWidget.class))
        .put(DifferentialDriveType.Instance, WidgetType.forAnnotatedWidget(DifferentialDriveWidget.class))
        .put(MecanumDriveType.Instance, WidgetType.forAnnotatedWidget(MecanumDriveWidget.class))
        .put(new SubsystemType(), createSubsystemLayoutType())
        .put(BasicSubsystemType.Instance, WidgetType.forAnnotatedWidget(BasicSubsystemWidget.class))
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
