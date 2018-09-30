[![Build Status](https://travis-ci.org/wpilibsuite/shuffleboard.svg?branch=master)](https://travis-ci.org/wpilibsuite/shuffleboard)
[![Build status](https://ci.appveyor.com/api/projects/status/auljw926o10sea4w/branch/master?svg=true)](https://ci.appveyor.com/project/AustinShalit/shuffleboard/branch/master)
[![codecov](https://codecov.io/gh/wpilibsuite/shuffleboard/branch/master/graph/badge.svg)](https://codecov.io/gh/wpilibsuite/shuffleboard)

# shuffleboard


## Structure

Shuffleboard is organized into three base projects: `api`, `app`, and `plugins`. `plugins` has additional
subprojects that the main app depends on to provide data types, widgets, and data sources for basic FRC use.

## Running

Shuffleboard is installed by the WPILib Eclipse Plugins. It can be launched from the WPILib menu in Eclipse.
It can also be run manually `java -jar C:\Users\<username\>wpilib\tools\Shuffleboard.jar`

### Requirements
- [JRE 11](http://jdk.java.net/11/). Java 11 is required.
No other version of Java is supported. Java 11 is installed on Windows by the
[FRC vscode extension](https://github.com/wpilibsuite/vscode-wpilib). Users on Mac or Linux will have to install Java 11
manually.

## Building

To run shuffleboard use the command `./gradlew :app:run`.

To build the APIs and utility classes used in plugin creation, use the command `./gradlew :api:shadowJar`

To build the Shuffleboard application, use the command `./gradlew :app:shadowJar`. By default, this will create an
executable JAR for your operating system. To build for another OS, add `-Pplatform={os}`, where `os` is one of the
following:
- `win32` for 32-bit Windows
- `win64` for 64-bit Windows
- `mac64` for macOS
- `linux64` for 64-bit Linux

### Requirements
- [JDK 11](http://jdk.java.net/11/). JDK 11 is required.
No other version of Java is supported.
