
import edu.wpi.first.wpilib.versioning.ReleaseType
import org.gradle.api.Project
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.0")
    }
}
plugins {
    `maven-publish`
    jacoco
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "2.0"
    id("com.github.johnrengelman.shadow") version "2.0.1"
    id("com.diffplug.gradle.spotless") version "3.5.1"
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
    }

    getWPILibVersion()?.let { version = it }

    // Spotless is used to lint and reformat source files.
    spotless {
        kotlinGradle {
            // Configure the formatting of the Gradle Kotlin DSL files (*.gradle.kts)
            ktlint("0.9.1")
            endWithNewline()
        }
        freshmark {
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
        format("extraneous") {
            target("Dockerfile", "*.sh", "*.yml")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
    }
}

allprojects {
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
        fun junitJupiter(name: String, version: String = "5.0.0") =
                create(group = "org.junit.jupiter", name = name, version = version)
        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(junitJupiter(name = "junit-jupiter-params"))
        "testRuntime"(create(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0"))
        fun testFx(name: String, version: String = "4.0.10-alpha") =
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

    tasks.withType<JavaCompile> {
        // UTF-8 characters are used in menus
        options.encoding = "UTF-8"
    }

    tasks.withType<FindBugs> {
        reports {
            xml.isEnabled = false
            emacs.isEnabled = true
        }
        finalizedBy(task("${name}Report") {
            mustRunAfter(this@withType)
            doLast {
                this@withType
                    .reports
                    .emacs
                    .destination
                    .takeIf { it.exists() }
                    ?.readText()
                    .takeIf { !it.isNullOrBlank() }
                    ?.also { logger.warn(it) }
            }
        })
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }
    afterEvaluate {
        val junitPlatformTest : JavaExec by tasks
        jacoco {
            applyTo(junitPlatformTest)
        }
        task<JacocoReport>("jacocoJunit5TestReport") {
            executionData(junitPlatformTest)
            sourceSets(java.sourceSets["main"])
            sourceDirectories = files(java.sourceSets["main"].allSource.srcDirs)
            classDirectories = files(java.sourceSets["main"].output)
        }
    }

    /*
     * Run UI tests in headless mode on Jenkins or when the `visibleUiTests` property is not set.
     */
    if (project.hasProperty("jenkinsBuild") || !project.hasProperty("visibleUiTests")) {
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

    if (project.hasProperty("jenkinsBuild")) {
        junitPlatform {
            filters {
                tags {
                    exclude("NonJenkinsTest")
                }
            }
        }
    }

    tasks.withType<Javadoc> {
        isFailOnError = false
    }
}

project(":app") {
    apply {
        plugin("com.github.johnrengelman.shadow")
    }
    val sourceJar = task<Jar>("sourceJar") {
        description = "Creates a JAR that contains the source code."
        from(java.sourceSets["main"].allSource)
        classifier = "sources"
    }
    val javadocJar = task<Jar>("javadocJar") {
        dependsOn("javadoc")
        description = "Creates a JAR that contains the javadocs."
        from(java.docsDir)
        classifier = "javadoc"
    }
    publishing {
        publications {
            create<MavenPublication>("app") {
                groupId = "edu.wpi.first.shuffleboard"
                artifactId = "app"
                getWPILibVersion()?.let { version = it }
                val shadowJar: ShadowJar by tasks
                artifact (shadowJar) {
                    classifier = null
                }
                artifact(sourceJar)
                artifact(javadocJar)
            }
        }
    }
}

project(":api") {
    apply {
        plugin("com.github.johnrengelman.shadow")
    }
    val sourceJar = task<Jar>("sourceJar") {
        description = "Creates a JAR that contains the source code."
        from(java.sourceSets["main"].allSource)
        classifier = "sources"
    }
    val javadocJar = task<Jar>("javadocJar") {
        dependsOn("javadoc")
        description = "Creates a JAR that contains the javadocs."
        from(java.docsDir)
        classifier = "javadoc"
    }
    publishing {
        publications {
            create<MavenPublication>("api") {
                groupId = "edu.wpi.first.shuffleboard"
                artifactId = "api"
                getWPILibVersion()?.let { version = it }
                afterEvaluate {
                    from(components["java"])
                }
                artifact(sourceJar)
                artifact(javadocJar)
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
