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
    api(group = "edu.wpi.first.ntcore", name = "ntcore-java", version = "4.+")
    api(group = "eu.hansolo", name = "Medusa", version = "7.9") // Note the capital 'M' -- lowercase is a much older version!
    api(group = "com.cedarsoft.commons", name = "version", version = "8.3.1")
    implementation(group = "edu.wpi.first.wpiutil", name = "wpiutil-java", version = "3.+")
    implementation(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = "4.+", classifier = "all")
}
