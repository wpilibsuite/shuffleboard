[![Build Status](https://dev.azure.com/wpilib/DesktopTools/_apis/build/status/wpilibsuite.shuffleboard)](https://dev.azure.com/wpilib/DesktopTools/_build/latest?definitionId=11)
[![codecov](https://codecov.io/gh/wpilibsuite/shuffleboard/branch/master/graph/badge.svg)](https://codecov.io/gh/wpilibsuite/shuffleboard)

# shuffleboard


## Structure

Shuffleboard is organized into three base projects: `api`, `app`, and `plugins`. `plugins` has additional
subprojects that the main app depends on to provide data types, widgets, and data sources for basic FRC use.

## Running

Shuffleboard is installed by the FRC vscode extension: [Installation Instructions](http://wpilib.screenstepslive.com/s/currentCS/m/java/l/1027503-installing-c-and-java-development-tools-for-frc).
It can be launched from the WPILib menu in Visual Studio Code (start tool).
It can also be run manually by running the shuffleboard.vbs in `c:\Users\public\frc<year>\tools` (Windows) or
shuffleboard.py in `~/frc<year>/tools` (Linux or Mac).

### Requirements
- [JRE 11](http://jdk.java.net/11/). Java 11 is required.
No other version of Java is supported. Java 11 is installed on Windows by the
[WPILib installer](https://github.com/wpilibsuite/allwpilib/releases). Users on Mac or Linux will have to install Java 11
manually.

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
- [JDK 11](http://jdk.java.net/11/). JDK 11 is required.
No other version of Java is supported.
