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
    pmd
    findbugs
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
        plugin("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin")
    }
    repositories {
        mavenCentral()
    }

    dependencies {
        testCompile(group = "junit", name = "junit", version = "+")
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
    gradleVersion = "4.0.2"
}
