
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
apply {
    plugin("org.junit.platform.gradle.plugin")
}
plugins {
    java
    application
    idea
    checkstyle
    `maven-publish`
    pmd
    findbugs
    jacoco
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "1.6"
    id("com.github.johnrengelman.shadow") version "2.0.1"
}

repositories {
    mavenCentral()
}

dependencies {
    compile(group = "com.google.guava", name = "guava", version = "21.0")
    compile(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")
    compile(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    compile(group = "edu.wpi.first.wpilib.networktables.java", name = "NetworkTables", version = "+", classifier = "desktop")
    compile(group = "com.google.code.gson", name = "gson", version = "2.8.1")

    fun junitJupiter(name: String, version: String = "5.0.0-M4") =
            create(group = "org.junit.jupiter", name = name, version = version)
    testCompile(junitJupiter(name = "junit-jupiter-api"))
    testCompile(junitJupiter(name = "junit-jupiter-engine"))
    testRuntime(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0-M4")
    fun testFx(name: String, version: String = "4.0.+") = create(group = "org.testfx", name = name, version = version)
    testCompile(testFx(name = "testfx-core"))
    testCompile(testFx(name = "testfx-junit5"))
    testRuntime(testFx(name = "openjfx-monocle", version = "8u76-b04"))
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

val theMainClassName = "edu.wpi.first.shuffleboard.Shuffleboard"

application {
    mainClassName = theMainClassName
}

val sourceJar = task<Jar>("sourceJar") {
    description = "Creates a JAR that contains the source code."
    from(java.sourceSets["main"].allSource)
    classifier = "sources"
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
    gradleVersion = "4.0.2"
}

fun Project.`junitPlatform`(action: JUnitPlatformExtension.() -> Unit = {}) =
        extensions.configure("junitPlatform", action)
