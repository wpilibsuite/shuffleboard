plugins {
    `java-library`
}

description = """
Public API for writing plugins for shuffleboard.
""".trimMargin()

dependencies {
    api(group = "com.google.guava", name = "guava", version = "21.0")
    api(group = "com.google.code.gson", name = "gson", version = "2.8.2")
    api(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")
    api(group = "org.controlsfx", name = "controlsfx", version = "9.0.0")
    api(group = "de.codecentric.centerdevice", name = "javafxsvg", version = "1.2.1")
    api(group = "eu.hansolo", name = "Medusa", version = "7.9") // Note the capital 'M' -- lowercase is a much older version!
    api(group = "com.jfoenix", name = "jfoenix", version = "9.0.8")
    api(group = "com.github.zafarkhaja", name = "java-semver", version = "0.9.0")
}
