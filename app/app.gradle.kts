import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import java.time.Instant

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

repositories {
    maven {
        setUrl("https://dl.bintray.com/samcarlberg/maven-artifacts/")
    }
}

dependencies {
    // JavaFX dependencies
    javafx("base")
    javafx("controls")
    javafx("fxml")
    javafx("graphics")
    // Note: we don't use these modules, but third-party plugins might
    javafx("media")
    javafx("swing")
    javafx("web")

    nativeProject(path = ":api")
    nativeProject(path = ":plugins:base")
    nativeProject(path = ":plugins:cameraserver")
    nativeProject(path = ":plugins:networktables")
    nativeProject(path = ":plugins:powerup")
    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")
    compile(group = "com.github.samcarlberg", name = "update-checker", version = "+")
    compile(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    testCompile(project("test_plugins"))
}

val theMainClassName = "edu.wpi.first.shuffleboard.app.Main"

application {
    mainClassName = theMainClassName
    applicationDefaultJvmArgs = listOf(
            "-Xverify:none",
            "-Dprism.order=d3d,es2,sw"
    )
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = theMainClassName
    }
}

/**
 * Make tests get the most recent version of the test plugin jar.
 */
tasks.withType<Test> {
    dependsOn(project("test_plugins").tasks["jar"])
}

val nativeShadowTasks = NativePlatforms.values().map { platform ->
    tasks.create<ShadowJar>("shadowJar-platform_${platform.platformName}") {
        classifier = platform.platformName
        configurations = listOf(
                project.configurations.compile,
                project.configurations.getByName(platform.platformName)
        )
        from(
                java.sourceSets["main"].output,
                project(":api").java.sourceSets["main"].output,
                project(":plugins:base").java.sourceSets["main"].output,
                project(":plugins:cameraserver").java.sourceSets["main"].output,
                project(":plugins:networktables").java.sourceSets["main"].output,
                project(":plugins:powerup").java.sourceSets["main"].output
        )
    }
}

tasks.create("shadowJarAllPlatforms") {
    nativeShadowTasks.forEach {
        this.dependsOn(it)
    }
}

tasks.withType<ShadowJar> {
    exclude("module-info.class")
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
            artifactId = "shuffleboard"
            nativeShadowTasks.forEach {
                artifact(it) {
                    classifier = it.classifier
                }
            }
            artifact(sourceJar)
            artifact(javadocJar)
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Implementation-Version"] = version ?: "v0.0.0"
        attributes["Built-Date"] = Instant.now().toString()
    }
}

/**
 * Lets tests use the output of the test_plugins build.
 */
java.sourceSets["test"].resources.srcDirs += File(project("test_plugins").buildDir, "libs")

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
