
import org.gradle.jvm.tasks.Jar

plugins {
    application
}
apply {
    plugin("com.github.johnrengelman.shadow")
    plugin("maven-publish")
}

description = """
All of the application specific code that makes shuffleboard run.
""".trimMargin()

dependencies {
    compile(project(":api"))
    compile(project(path = ":plugins:base"))
    compile(project(path = ":plugins:cameraserver"))
    compile(project(path = ":plugins:networktables"))
    compile(group = "com.google.code.gson", name = "gson", version = "2.8.1")
    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")

    runtime(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = "3.1.7-20170808143930-12-gccfeab5", classifier = "all")
}

val theMainClassName = "edu.wpi.first.shuffleboard.app.Shuffleboard"

application {
    mainClassName = theMainClassName
}

tasks.withType<Jar> {
    getWPILibVersion()?.let { version = it }
    manifest {
        attributes(mapOf(
            "Implementation-Version" to getWPILibVersion(),
            "Main-Class" to theMainClassName
        ).filterValues { it != null })
    }
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
