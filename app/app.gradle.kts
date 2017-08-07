import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.gradle.script.lang.kotlin.create
import org.gradle.script.lang.kotlin.invoke

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
    fun testFx(name: String, version: String = "4.0.+") =
        create(group = "org.testfx", name = name, version = version)
    testCompile(testFx(name = "testfx-core"))
    testCompile(testFx(name = "testfx-junit"))
    testRuntime(testFx(name = "openjfx-monocle", version = "8u76-b04"))
}

val theMainClassName = "edu.wpi.first.shuffleboard.Shuffleboard"

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

tasks {
    "shadowJar"(ShadowJar::class) {
        classifier = null
    }
}

tasks.withType<Test> {
    /*
     * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless argument
     */
    if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
        println("Running UI Tests Headless")

        jvmArgs = listOf(
            "-Djava.awt.headless=true",
            "-Dtestfx.robot=glass",
            "-Dtestfx.headless=true",
            "-Dprism.order=sw",
            "-Dprism.text=t2k"
        )
        useJUnit {
            this as JUnitOptions
            excludeCategories("edu.wpi.first.shuffleboard.NonHeadlessTests")
        }
    }
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null