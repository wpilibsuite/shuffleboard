import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.jvm.tasks.Jar
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import groovy.lang.GroovyObject

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
    nativeProject(path = ":plugins:powerup", type = "runtimeOnly")

    compile(group = "de.huxhorn.lilith", name = "de.huxhorn.lilith.3rdparty.junique", version = "1.0.4")
    compile(group = "org.apache.commons", name = "commons-csv", version = "1.5")
    testCompile(project("test_plugins"))
    testCompile(project(":api-test-util"))
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
    tasks.create<ShadowJar>("shadowJar-${platform.platformName}") {
        classifier = platform.platformName
        configurations = listOf(
                project.configurations.getByName("runtimeClasspath"),
                project.configurations.getByName(platform.platformName)
        )
        with(tasks.jar.get() as CopySpec)
    }
}

tasks.create("shadowJarAllPlatforms") {
    nativeShadowTasks.forEach {
        this.dependsOn(it)
    }
}

tasks.withType<ShadowJar>().configureEach {
    exclude("module-info.class")
}

if (System.getenv()["RUN_AZURE_ARTIFACTORY_RELEASE"] != null) {
    artifactory {
        publish(delegateClosureOf<PublisherConfig> {
            defaults(delegateClosureOf<GroovyObject> {
                invokeMethod("publications", "app")
            })
        })
    }
}

publishing {
    publications {
        create<MavenPublication>("app") {
            groupId = "edu.wpi.first.shuffleboard"
            artifactId = "shuffleboard"
            version = project.version as String
            nativeShadowTasks.forEach {
                artifact(it) {
                    classifier = it.classifier
                }
            }
        }
    }
}

/**
 * Lets tests use the output of the test_plugins build.
 */
sourceSets["test"].resources.srcDirs.add(File(project("test_plugins").buildDir, "libs"))
