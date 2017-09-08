import edu.wpi.first.wpilib.versioning.ReleaseType
import groovy.util.Node
import groovy.util.XmlParser
import groovy.xml.XmlUtil
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
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
    id("com.diffplug.gradle.spotless") version "3.5.1"
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
    }

    repositories {
        mavenCentral()
    }

    // Spotless is used to lint and reformat source files.
    spotless {
        kotlinGradle {
            // Configure the formatting of the Gradle Kotlin DSL files (*.gradle.kts)
            ktlint("0.9.1")
            endWithNewline()
        }
    }
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

    dependencies {
        fun junitJupiter(name: String, version: String = "5.0.0-RC2") =
                create(group = "org.junit.jupiter", name = name, version = version)
        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(junitJupiter(name = "junit-jupiter-params"))
        "testRuntime"(create(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0-RC2"))
        fun testFx(name: String, version: String = "4.0.+") =
                create(group = "org.testfx", name = name, version = version)
        "testCompile"(testFx(name = "testfx-core"))
        "testCompile"(testFx(name = "testfx-junit5"))
        "testRuntime"(testFx(name = "openjfx-monocle", version = "8u76-b04"))
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
    afterEvaluate {
        val junitPlatformTest by tasks
        jacoco {
            applyToHelper(junitPlatformTest)
        }
        task<JacocoReport>("jacocoJunit5TestReport") {
            executionData(junitPlatformTest)
            sourceSets(java.sourceSets["main"])
            sourceDirectories = files(java.sourceSets["main"].allSource.srcDirs)
            classDirectories = files(java.sourceSets["main"].output)
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
}

/*
 * Add a JacocoCoverage task that merges the coverage of all of all subprojects into one report.
 * This improves code coverage as it will also capture coverage for tests that cross project barriers.
 * http://csiebler.github.io/blog/2014/02/09/multi-project-code-coverage-using-gradle-and-jacoco/
 */
task<JacocoReport>("codeCoverageReport") {
    executionData(fileTree(rootDir.absolutePath).include("**/build/jacoco/*.exec"))
    subprojects {
        this@task.sourceSets(java.sourceSets["main"])
        this@task.dependsOn(tasks.getByName("test"))
    }
    reports {
        xml.isEnabled = true
        html.isEnabled = true
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
 * Workaround fix for calling [org.gradle.testing.jacoco.plugins.JacocoPluginExtension.applyTo]
 *
 * [Issue details here](https://github.com/gradle/kotlin-dsl/issues/458)
 */
fun org.gradle.testing.jacoco.plugins.JacocoPluginExtension.applyToHelper(task : Task) {
    val method = this::class.java.getMethod("applyTo", Task::class.java)
    method.invoke(this, task)
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

/**
 * Retrieves the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
val Project.`junitPlatform`: org.junit.platform.gradle.plugin.JUnitPlatformExtension get() =
    extensions.getByName("junitPlatform") as org.junit.platform.gradle.plugin.JUnitPlatformExtension

/**
 * Configures the [junitPlatform][org.junit.platform.gradle.plugin.JUnitPlatformExtension] project extension.
 */
fun Project.`junitPlatform`(configure: org.junit.platform.gradle.plugin.JUnitPlatformExtension.() -> Unit) =
    extensions.configure("junitPlatform", configure)
