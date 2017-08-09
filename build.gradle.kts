import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import edu.wpi.first.wpilib.versioning.ReleaseType
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.tasks.Jar
import org.gradle.script.lang.kotlin.*
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.junit.platform.gradle.plugin.JUnitPlatformExtension
import java.io.File

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0-RC2")
    }
}
plugins {
    `maven-publish`
    jacoco
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "1.6"
    id("com.github.johnrengelman.shadow") version "2.0.1"
}

subprojects {
    apply {
        plugin("java")
        plugin("checkstyle")
        plugin("pmd")
        plugin("findbugs")
        plugin("jacoco")
        plugin("maven-publish")
        plugin("org.junit.platform.gradle.plugin")
        plugin("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin")
    }
    repositories {
        mavenCentral()
    }

    dependencies {
        fun junitJupiter(name: String, version: String = "5.0.0-M4") =
            create(group = "org.junit.jupiter", name = name, version = version)
        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(create(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0-M4"))
    }

    checkstyle {
        configFile = file("$rootDir/checkstyle.xml")
        toolVersion = "8.1"
    }

    pmd {
        isConsoleOutput = true
        sourceSets = setOf(java.sourceSets["main"], java.sourceSets["test"])
        reportsDir = file("${project.buildDir}/reports/pmd")
        ruleSetFiles = files(file("$rootDir/pmd-ruleset.xml"))
        ruleSets = emptyList()
    }

    findbugs {
        sourceSets = setOf(java.sourceSets["main"], java.sourceSets["test"])
        excludeFilter = file("$rootDir/findBugsSuppressions.xml")
        effort = "max"
    }

    fun printReportSafe(xmlReport: File) {
        if (xmlReport.exists()) {
            val bugs = (XmlParser().parse(xmlReport)["BugInstance"]) as Collection<*>
            bugs.forEach {
                println(XmlUtil.serialize(it as Node))
            }
        }
    }

    val findbugsMain: FindBugs by tasks
    val findbugsMainReport = task("findbugsMainReport") {
        doLast {
            printReportSafe(findbugsMain.reports.xml.destination)
        }
    }
    val findbugsTest: FindBugs by tasks
    val findbugsTestReport = task("findBugsTestReport") {
        doLast {
            printReportSafe(findbugsTest.reports.xml.destination)
        }
    }
    findbugsMain.finalizedBy(findbugsMainReport)
    findbugsTest.finalizedBy(findbugsTestReport)

    tasks.withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
}

configure(setOf(project(":app"))) {
    apply {
        plugin("com.github.johnrengelman.shadow")
    }
    val sourceJar = task<Jar>("sourceJar") {
        description = "Creates a JAR that contains the source code."
        from(java.sourceSets["main"].allSource)
        classifier = "sources"
    }
    publishing {
        publications {
            create<MavenPublication>("shadow") {
                groupId = "edu.wpi.first.shuffleboard"
                artifactId = "Shuffleboard"
                getWPILibVersion()?.let { version = it }
                shadow.component(this)
                from(components["java"])
                artifact(sourceJar)
            }
        }
    }
}

// Ensure that the WPILibVersioningPlugin is setup by setting the release type, if releaseType wasn't
// already specified on the command line
if (!hasProperty("releaseType")) {
    WPILibVersion {
        releaseType = ReleaseType.DEV
    }
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null

task<Wrapper>("wrapper") {
    gradleVersion = "4.1"
}

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention get() =
    convention.getPluginByName("java")

/**
 * Retrieves the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
val Project.`checkstyle`: org.gradle.api.plugins.quality.CheckstyleExtension get() =
    extensions.getByName("checkstyle") as org.gradle.api.plugins.quality.CheckstyleExtension

/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit) =
    extensions.configure("checkstyle", configure)

/**
 * Retrieves the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
val Project.`pmd`: org.gradle.api.plugins.quality.PmdExtension get() =
    extensions.getByName("pmd") as org.gradle.api.plugins.quality.PmdExtension

/**
 * Configures the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
fun Project.`pmd`(configure: org.gradle.api.plugins.quality.PmdExtension.() -> Unit) =
    extensions.configure("pmd", configure)

/**
 * Retrieves the [findbugs][org.gradle.api.plugins.quality.FindBugsExtension] project extension.
 */
val Project.`findbugs`: org.gradle.api.plugins.quality.FindBugsExtension get() =
    extensions.getByName("findbugs") as org.gradle.api.plugins.quality.FindBugsExtension

/**
 * Configures the [findbugs][org.gradle.api.plugins.quality.FindBugsExtension] project extension.
 */
fun Project.`findbugs`(configure: org.gradle.api.plugins.quality.FindBugsExtension.() -> Unit) =
    extensions.configure("findbugs", configure)
