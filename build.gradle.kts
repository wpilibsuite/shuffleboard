import com.diffplug.spotless.FormatterStep
import com.github.spotbugs.SpotBugsTask
import com.github.spotbugs.SpotBugsExtension
import edu.wpi.first.wpilib.versioning.ReleaseType
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.time.Instant

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
}
plugins {
    `maven-publish`
    jacoco
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "2.3"
    id("com.github.johnrengelman.shadow") version "4.0.3"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("com.github.spotbugs") version "1.6.4"
    id("com.google.osdetector") version "1.4.0"
}

// Ensure that the WPILibVersioningPlugin is setup by setting the release type, if releaseType wasn't
// already specified on the command line
if (!hasProperty("releaseType")) {
    WPILibVersion {
        releaseType = ReleaseType.DEV
    }
}

// Only load the project version once, then share it
val projectVerion = getWPILibVersion()

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
    }

    version = projectVerion

    // Note: plugins should override this
    tasks.withType<Jar>().configureEach {
        manifest {
            attributes["Implementation-Version"] = project.version as String
            attributes["Built-Date"] = Instant.now().toString()
        }
    }

    // Spotless is used to lint and reformat source files.
    spotless {
        kotlinGradle {
            ktlint("0.24.0")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
        format("extraneous") {
            target("*.sh", "*.yml")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
        format("markdown") {
            target("*.md")
            // Default timeWhitespace() doesn't respect lines ending with two spaces for a tight line break
            // So we have to implement it ourselves
            class TrimTrailingSpaces : FormatterStep {
                override fun getName(): String = "trimTrailingSpaces"

                override fun format(rawUnix: String?, file: File?): String? {
                    if (rawUnix == null || file == null) {
                        return null
                    }
                    return rawUnix.split('\n')
                            .joinToString(separator = "\n", transform = this::formatLine)
                }

                fun formatLine(line: String): String {
                    if (!line.endsWith(" ")) {
                        // No trailing whitespace
                        return line
                    }
                    if (line.matches(Regex("^.*[^ \t] {2}$"))) {
                        // Ends with two spaces - it's a tight line break, so leave it
                        return line
                    }
                    val endsWithMoreThanTwoSpaces = Regex("^(.*[^ \t]) {3,}^")
                    val match = endsWithMoreThanTwoSpaces.matchEntire(line)
                    if (match != null) {
                        // Ends with at least 3 spaces
                        // Trim the excess, but leave two spaces at the end for a tight line break
                        return match.groupValues[1] + "  "
                    }
                    if (line.endsWith(" ")) {
                        // Ends with a single space - remove it
                        return line.substring(0, line.length - 1)
                    }
                    // Not sure how we got here; every case should have been covered.
                    // Print an error but do not change the line
                    System.err.println("Could not trim whitespace from line '$line'")
                    return line
                }
            }
            addStep(TrimTrailingSpaces())
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
        plugin("com.github.spotbugs")
        plugin("jacoco")
        plugin("maven-publish")
    }
    repositories {
        mavenCentral()
    }

    createNativeConfigurations()

    dependencies {
        fun junitJupiter(name: String, version: String = "5.2.0") =
                create(group = "org.junit.jupiter", name = name, version = version)
        "compileOnly"(create(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1"))
        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(junitJupiter(name = "junit-jupiter-params"))
        "testCompile"(group = "org.junit-pioneer", name = "junit-pioneer", version = "0.3.0")
        "testRuntime"(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0")
        fun testFx(name: String, version: String = "4.0.13-alpha") =
                create(group = "org.testfx", name = name, version = version)
        "testCompile"(testFx(name = "testfx-core"))
        "testCompile"(testFx(name = "testfx-junit5"))
        "testRuntime"(testFx(name = "openjfx-monocle", version = "jdk-9+181"))

        javafx("base")
        javafx("controls")
        javafx("fxml")
        javafx("graphics")
    }

    checkstyle {
        toolVersion = "8.12"
    }

    pmd {
        toolVersion = "6.7.0"
        isConsoleOutput = true
        sourceSets = setOf(java.sourceSets["main"])
        reportsDir = file("${project.buildDir}/reports/pmd")
        ruleSetFiles = files(file("$rootDir/pmd-ruleset.xml"))
        ruleSets = emptyList()
    }

    spotbugs {
        toolVersion = "3.1.7"
        sourceSets = setOf(java.sourceSets["main"], java.sourceSets["test"])
        excludeFilter = file("$rootDir/findBugsSuppressions.xml")
        effort = "max"
    }

    tasks.withType<JavaCompile> {
        // UTF-8 characters are used in menus
        options.encoding = "UTF-8"
    }

    tasks.withType<SpotBugsTask> {
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

    jacoco {
        toolVersion = "0.8.2"
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.isEnabled = true
            html.isEnabled = true
        }
    }

    tasks.withType<Test> {
        // TODO: re-enable when TestFX (or the underlying JavaFX problem) is fixed
        println("UI tests will not be run due to TestFX being broken when headless on Java 10.")
        println("See: https://github.com/javafxports/openjdk-jfx/issues/66")
        // Link: https://github.com/javafxports/openjdk-jfx/issues/66
        useJUnitPlatform {
            excludeTags("UI")
        }
    }

    tasks.withType<Javadoc> {
        isFailOnError = false
    }
}

project(":api") {
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
                version = project.version.toString()
                afterEvaluate {
                    from(components["java"])
                }
                artifact(sourceJar)
                artifact(javadocJar)
            }
        }
    }
}

/**
 * @return publishVersion property if exists, otherwise
 * [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or fallback
 * if that value is the empty string.
 */
fun getWPILibVersion(fallback: String = "0.0.0"): String {
    if (project.hasProperty("publishVersion")) {
        val publishVersion: String by project
        return publishVersion
    } else if (WPILibVersion.version != "") {
        return WPILibVersion.version
    } else {
        return fallback
    }
}

tasks.withType<Wrapper>().configureEach {
    gradleVersion = "5.0"
}

/**
 * Retrieves the [java][org.gradle.api.plugins.JavaPluginConvention] project convention.
 */
val Project.`java`: org.gradle.api.plugins.JavaPluginConvention
    get() =
        convention.getPluginByName("java")

/**
 * Retrieves the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
val Project.`checkstyle`: org.gradle.api.plugins.quality.CheckstyleExtension
    get() =
        extensions.getByName("checkstyle") as org.gradle.api.plugins.quality.CheckstyleExtension

/**
 * Configures the [checkstyle][org.gradle.api.plugins.quality.CheckstyleExtension] project extension.
 */
fun Project.`checkstyle`(configure: org.gradle.api.plugins.quality.CheckstyleExtension.() -> Unit) =
        extensions.configure("checkstyle", configure)

/**
 * Retrieves the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
val Project.`pmd`: org.gradle.api.plugins.quality.PmdExtension
    get() =
        extensions.getByName("pmd") as org.gradle.api.plugins.quality.PmdExtension

/**
 * Configures the [pmd][org.gradle.api.plugins.quality.PmdExtension] project extension.
 */
fun Project.`pmd`(configure: org.gradle.api.plugins.quality.PmdExtension.() -> Unit) =
        extensions.configure("pmd", configure)

val Project.`spotbugs`: SpotBugsExtension
    get() =
        extensions.getByName("spotbugs") as SpotBugsExtension

fun Project.`spotbugs`(configure: SpotBugsExtension.() -> Unit) =
        extensions.configure("spotbugs", configure)
