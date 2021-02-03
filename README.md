![CI](https://github.com/wpilibsuite/shuffleboard/workflows/CI/badge.svg)

# Shuffleboard


## Structure

Shuffleboard is organized into three base projects: `api`, `app`, and `plugins`. `plugins` has additional
subprojects that the main app depends on to provide data types, widgets, and data sources for basic FRC use.

## Running

Shuffleboard is installed by the FRC vscode extension: [Installation Instructions](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html).
It can be launched from the WPILib menu in Visual Studio Code (start tool).
It can also be run manually by running the shuffleboard.vbs in `c:\Users\public\wpilib\<year>\tools` (Windows) or
shuffleboard.py in `~/wpilib/<year>/tools` (Linux or Mac).

### Requirements
- [JRE 11](https://adoptopenjdk.net/). Java 11 is required.
No other version of Java is supported. Java 11 is installed by the
[WPILib installer](https://github.com/wpilibsuite/allwpilib/releases).

## Building

To run shuffleboard use the command `./gradlew :app:run`.

To build the APIs and utility classes used in plugin creation, use the command `./gradlew :api:shadowJar`

To build the Shuffleboard application, use the command `./gradlew :app:shadowJar`. By default, this will create an
executable JAR for your operating system. To build for another OS, use one of the platform-specific builds:

| OS | Command |
|---|---|
| Windows 64-bit | `./gradlew :app:shadowJar-win64` |
| Windows 32-bit | `./gradlew :app:shadowJar-win32` |
| Mac | `./gradlew :app:shadowJar-mac64` |
| Linux 64-bit | `./gradlew :app:shadowJar-linux64` |

Only the listed platforms are supported

To build _all_ platform-specific JARs at once, use the command `./gradlew :app:shadowJarAllPlatforms`

### Requirements
- [JDK 11](https://adoptopenjdk.net/). JDK 11 is required.
No other version of Java is supported.
