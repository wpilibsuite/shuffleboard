plugins {
    `java-library`
}

description = """
Public API for writing plugins for shuffleboard.
""".trimMargin()

dependencies {
    api(group = "com.google.guava", name = "guava", version = "21.0")
    api(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")
    api(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    api(group = "de.codecentric.centerdevice", name = "javafxsvg", version = "1.2.1")
    api(group = "edu.wpi.first.wpilib.networktables.java", name = "NetworkTables", version = "+", classifier = "desktop")
}
