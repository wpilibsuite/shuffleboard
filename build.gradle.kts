import edu.wpi.first.wpilib.versioning.ReleaseType
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.tasks.Jar

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}
plugins {
    java
    application
    idea
    checkstyle
    `maven-publish`
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "1.6"
}
apply {
    plugin("pmd")
    plugin("findbugs")
    plugin("jacoco")
}

repositories {
    mavenCentral()
}

dependencies {
    compile(group = "com.google.guava", name = "guava", version = "21.0")
    compile(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")
    compile(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    compile("edu.wpi.first.wpilib.networktables.java:NetworkTables:+:desktop")
    compile(group = "com.google.code.gson", name = "gson", version = "2.8.1")

    testCompile(group = "com.google.guava", name = "guava-testlib", version = "21.0")
    testCompile("junit:junit:+")
    testCompile("org.testfx:testfx-core:4.0.+")
    testCompile("org.testfx:testfx-junit:4.0.+")
    testRuntime(group = "org.testfx", name = "openjfx-monocle", version = "8u76-b04")
}

checkstyle {
    configFile = file("$rootDir/checkstyle.xml")
    toolVersion = "6.19"
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

tasks.withType<Test> {
    testLogging {
        if (project.hasProperty("logTests") || project.hasProperty("jenkinsBuild")) {
            events("started", "passed", "skipped", "failed")
        } else {
            events("failed")
        }
        exceptionFormat = TestExceptionFormat.FULL
    }
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

application {
    mainClassName = "edu.wpi.first.shuffleboard.Shuffleboard"
}

val sourceJar = task<Jar>("sourceJar") {
    description = "Creates a JAR that contains the source code."
    from(java.sourceSets["main"].allSource)
    classifier = "sources"
}

tasks.withType<Jar> {
    if (WPILibVersion.version != "") {
        version = WPILibVersion.version
    }
}

publishing {
    publications {
        create<MavenPublication>("shadow") {
            groupId = "edu.wpi.first.shuffleboard"
            artifactId = "Shuffleboard"
            if (WPILibVersion.version != "") {
                version = WPILibVersion.version
            }
            from(components["java"])
            artifact(sourceJar)
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

task<Wrapper>("wrapper") {
    gradleVersion = "4.0.2"
}

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention get() =
convention.getPluginByName<org.gradle.api.plugins.JavaPluginConvention>("java")


/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit): Unit =
    extensions.configure("checkstyle", configure)

/**
 * Configures the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
fun Project.`pmd`(configure: org.gradle.api.plugins.quality.PmdExtension.() -> Unit): Unit =
    extensions.configure("pmd", configure)

/**
 * Configures the [findbugs][org.gradle.api.plugins.quality.FindBugsExtension] project extension.
 */
fun Project.`findbugs`(configure: org.gradle.api.plugins.quality.FindBugsExtension.() -> Unit): Unit =
    extensions.configure("findbugs", configure)

/**
 * Retrieves the [application][org.gradle.api.plugins.ApplicationPluginConvention] project convention.
 */
val Project.`application`: org.gradle.api.plugins.ApplicationPluginConvention get() =
convention.getPluginByName<org.gradle.api.plugins.ApplicationPluginConvention>("application")

/**
 * Configures the [application][org.gradle.api.plugins.ApplicationPluginConvention] project convention.
 */
fun Project.`application`(configure: org.gradle.api.plugins.ApplicationPluginConvention.() -> Unit): Unit =
    configure(`application`)

/**
 * Configures the [publishing][org.gradle.api.publish.PublishingExtension] project extension.
 */
fun Project.`publishing`(configure: org.gradle.api.publish.PublishingExtension.() -> Unit): Unit =
    extensions.configure("publishing", configure)

/**
 * Retrieves the [WPILibVersion][edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension] project extension.
 */
val Project.`WPILibVersion`: edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension get() =
extensions.getByName("WPILibVersion") as edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension

/**
 * Configures the [WPILibVersion][edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension] project extension.
 */
fun Project.`WPILibVersion`(configure: edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.() -> Unit): Unit =
    extensions.configure("WPILibVersion", configure)
