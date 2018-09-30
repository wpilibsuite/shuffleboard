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

repositories {
    maven {
        setUrl("https://dl.bintray.com/samcarlberg/maven-artifacts/")
    }
}

val platform: String by extra

dependencies {
    // JavaFX dependencies
    compile(javafx("base", platform))
    compile(javafx("controls", platform))
    compile(javafx("fxml", platform))
    compile(javafx("graphics", platform))
    // Note: we don't use these modules, but third-party plugins might
    runtime(javafx("media", platform))
    runtime(javafx("swing", platform))
    runtime(javafx("web", platform))

    compile(project(":api"))
    compile(project(path = ":plugins:base"))
    compile(project(path = ":plugins:cameraserver"))
    compile(project(path = ":plugins:networktables"))
    compile(project(path = ":plugins:powerup"))
    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")
    compile(group = "com.github.samcarlberg", name = "update-checker", version = "+")
    compile(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    testCompile(project("test_plugins"))
}

val theMainClassName = "edu.wpi.first.shuffleboard.app.Main"

application {
    mainClassName = theMainClassName
    applicationDefaultJvmArgs = listOf(
            "-Xverify:none",
            "-Dprism.order=d3d,es2,sw"
    )
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = theMainClassName
    }
}

/**
 * Make tests get the most recent version of the test plugin jar.
 */
tasks.withType<Test> {
    dependsOn(project("test_plugins").tasks["jar"])
}

/**
 * Lets tests use the output of the test_plugins build.
 */
java.sourceSets["test"].resources.srcDirs += File(project("test_plugins").buildDir, "libs")

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
