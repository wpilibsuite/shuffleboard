plugins {
    `java-library`
}

description = """
Public API for writing plugins for shuffleboard.
""".trimMargin()

dependencies {
    fun api(group: String, name: String, version: String?, classifier : String? = null) =
        add("api", create(group = group, name = name, version = version, classifier = classifier))
    api(group = "com.google.guava", name = "guava", version = "21.0")
    api(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")
    api(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    api(group = "edu.wpi.first.wpilib.networktables.java", name = "NetworkTables", version = "+", classifier = "desktop")
}