import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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
    compile(group = "com.google.code.gson", name = "gson", version = "2.8.1")
    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")
    fun testFx(name: String, version: String = "4.0.+") =
        create(group = "org.testfx", name = name, version = version)
    runtime(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = "3.1.7-20170808143930-12-gccfeab5", classifier = "all")
    testCompile(testFx(name = "testfx-core"))
    testCompile(testFx(name = "testfx-junit5"))
    testRuntime(testFx(name = "openjfx-monocle", version = "8u76-b04"))
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

/*
 * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless argument
 */
if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
    println("Running UI Tests Headless")
    junitPlatform {
        filters {
            tags {
                /*
                 * A category for UI tests that cannot run in headless mode, ie work properly with real windows
                 * but not with the virtualized ones in headless mode.
                 */
                exclude("NonHeadlessTests")
            }
        }
    }
    tasks {
        "junitPlatformTest"(JavaExec::class) {
            jvmArgs = listOf(
                "-Djava.awt.headless=true",
                "-Dtestfx.robot=glass",
                "-Dtestfx.headless=true",
                "-Dprism.order=sw",
                "-Dprism.text=t2k"
            )
        }
    }
}

tasks {
    "shadowJar"(ShadowJar::class) {
        classifier = null
    }
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
