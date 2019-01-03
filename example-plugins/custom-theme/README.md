# Example Plugin: Custom Theme

This plugin provides a custom theme for the Shuffleboard app that uses a modified version of the builtin `Midnight`
theme.

Note that if you just want a custom theme for Shuffleboard, you can place the CSS files in the
`~/Shuffleboard/themes/<YourThemeName>` directory without needing to write any code. This example is for showing
how themes can be provided by Shuffleboard plugins that _also_ provide other functionality.


## Building
`./gradlew :example-plugins:custom-theme:installPlugin` will build a jar file for the plugin and place it
in the Shuffleboard plugins directory `~/Shuffleboard/plugins`. The jar file can be generated with the `jar` task
if you want to build but not install the plugin.
