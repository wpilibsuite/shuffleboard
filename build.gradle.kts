import com.diffplug.spotless.FormatterStep
import com.github.spotbugs.SpotBugsTask
import com.github.spotbugs.SpotBugsExtension
import edu.wpi.first.wpilib.versioning.ReleaseType
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.wrapper.Wrapper
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.operation.DescribeOp
import java.time.Instant

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("org.ajoberstar:grgit:1.7.2")
    }
}
plugins {
    `maven-publish`
    jacoco
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "2.0"
    id("com.github.johnrengelman.shadow") version "2.0.1"
    id("com.diffplug.gradle.spotless") version "3.13.0"
    id("org.ajoberstar.grgit") version "1.7.2"
    id("com.github.spotbugs") version "1.6.2"
}

allprojects {
    apply {
        plugin("com.diffplug.gradle.spotless")
    }

    getWPILibVersion()?.let { version = it }

    // Spotless is used to lint and reformat source files.
    spotless {
        kotlinGradle {
            ktlint("0.24.0")
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
        plugin("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin")
    }
    repositories {
        mavenCentral()
    }

    dependencies {
        fun junitJupiter(name: String, version: String = "5.2.0") =
                create(group = "org.junit.jupiter", name = name, version = version)
        "compileOnly"(create(group = "com.google.code.findbugs", name = "annotations", version = "3.0.1"))
        "testCompile"(junitJupiter(name = "junit-jupiter-api"))
        "testCompile"(junitJupiter(name = "junit-jupiter-engine"))
        "testCompile"(junitJupiter(name = "junit-jupiter-params"))
        "testRuntime"(create(group = "org.junit.platform", name = "junit-platform-launcher", version = "1.0.0"))
        fun testFx(name: String, version: String = "4.0.13-alpha") =
                create(group = "org.testfx", name = name, version = version)
        "testCompile"(testFx(name = "testfx-core"))
        "testCompile"(testFx(name = "testfx-junit5"))
        "testRuntime"(testFx(name = "openjfx-monocle", version = "jdk-9+181"))
    }

    checkstyle {
        configFile = file("$rootDir/checkstyle.xml")
        toolVersion = "8.11"
    }

    pmd {
        toolVersion = "6.5.0"
        isConsoleOutput = true
        sourceSets = setOf(java.sourceSets["main"])
        reportsDir = file("${project.buildDir}/reports/pmd")
        ruleSetFiles = files(file("$rootDir/pmd-ruleset.xml"))
        ruleSets = emptyList()
    }

    spotbugs {
        toolVersion = "3.1.6"
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
                artifact(shadowJar) {
                    classifier = null
                }
                artifact(sourceJar)
                artifact(javadocJar)
            }
        }
    }

    version = getWPILibVersion() ?: getVersionFromGitTag(fallback = "0.0.0") // fall back to git describe if no WPILib version is set
    tasks.withType<Jar> {
        manifest {
            attributes["Implementation-Version"] = version
            attributes["Built-Date"] = Instant.now().toString()
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

    version = getWPILibVersion() ?: getVersionFromGitTag(fallback = "v0.0.0")
    tasks.withType<Jar> {
        manifest {
            attributes["Implementation-Version"] = version
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
    gradleVersion = "4.9"
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

/**
 * Gets the build version from git-describe. This is a combination of the most recent tag, the number of commits since
 * that tag, and the abbreviated hash of the most recent commit, in this format: `<tag>-<n>-<hash>`; for example,
 * v1.0.0-11-9ab123f when the most recent tag is `"v1.0.0"`, with 11 commits since that tag, and the most recent commit
 * hash starting with `9ab123f`.
 *
 * @param fallback the version string to fall back to if git-describe fails. Default value is `"v0.0.0"`.
 *
 * @see <a href="https://git-scm.com/docs/git-describe">git-describe documentation</a>
 */
fun getVersionFromGitTag(fallback: String = "v0.0.0"): String = try {
    val git = Grgit.open()
    DescribeOp(git.repository).call() ?: fallback
} catch (e: GrgitException) {
    logger.log(LogLevel.WARN, "Cannot get the version from git-describe, falling back to $fallback", e)
    fallback
}
