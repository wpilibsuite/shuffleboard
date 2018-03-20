package edu.wpi.first.shuffleboard.plugin.powerup;

import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.plugin.Requires;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.powerup.widget.PowerupFieldWidget;

import com.google.common.collect.ImmutableList;

import java.util.List;

@Description(
    group = "edu.wpi.first.shuffleboard",
    name = "Powerup",
    version = PowerupPluginVersion.VERSION,
    summary = "Adds widgets for viewing FMS Info for the 2018 FRC game POWER UP"
)
@Requires(group = "edu.wpi.first.shuffleboard", name = "Base", minVersion = "1.0.0")
public class PowerupPlugin extends Plugin {

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(WidgetType.forAnnotatedWidget(PowerupFieldWidget.class));
  }

}
