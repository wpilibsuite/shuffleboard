[![Build Status](https://travis-ci.org/wpilibsuite/shuffleboard.svg?branch=master)](https://travis-ci.org/wpilibsuite/shuffleboard)
[![Build status](https://ci.appveyor.com/api/projects/status/auljw926o10sea4w/branch/master?svg=true)](https://ci.appveyor.com/project/AustinShalit/shuffleboard/branch/master)
[![codecov](https://codecov.io/gh/wpilibsuite/shuffleboard/branch/master/graph/badge.svg)](https://codecov.io/gh/wpilibsuite/shuffleboard)

# shuffleboard


## Structure

Shuffleboard is organized into three base projects: `api`, `app`, and `plugins`. `plugins` has additional
subprojects that the main app depends on to provide data types, widgets, and data sources for basic FRC use.

## Running

To run shuffleboard use the command `./gradlew :app:run`.

### Requirements
- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

## Building

To build the APIs and utility classes used in plugin creation, use the command ./gradlew :api:shadowJar
To build the Shuffleboard application, use the command ./gradlew :app:shadowJar

### Requirements
- [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
