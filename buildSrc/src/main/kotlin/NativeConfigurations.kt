import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

enum class NativePlatforms(val platformName: String) {
    WIN32("win32"),
    WIN64("win64"),
    MAC("mac64"),
    LINUX("linux64");

    companion object {
        fun forName(platformName: String): NativePlatforms {
            return values().find { it.platformName == platformName } ?: throw NoSuchElementException(platformName)
        }
    }
}

/**
 * Creates a [Configuration] for the given platform.  If the given platform is also the current platform (i.e. the
 * operating system running the Gradle build), then the `compileOnly`, `runtimeOnly`, and `testCompile` configurations
 * will extend from the native configuration for purposes of being able to run the application and tests.
 */
fun Project.nativeConfig(platformName: String): Configuration {
    val configuration = configurations.create(platformName)
    if (platformName == currentPlatform) {
        configurations.getByName("compileOnly").extendsFrom(configuration)
        configurations.getByName("runtimeOnly").extendsFrom(configuration)
        configurations.getByName("testCompile").extendsFrom(configuration)
    }
    return configuration
}

/**
 * Creates all the native configurations for the project.
 */
fun Project.createNativeConfigurations() = forEachPlatform { platform -> nativeConfig(platform.platformName) }

/**
 * Adds a dependency on a native (platform-specific) artifact.
 *
 * @param group              the group ID of the artifact
 * @param name               the name of the artifact
 * @param version            the version of the artifact (wildcards are supported)
 * @param classifierFunction a function that takes a native platform and returns the classifier
 *                           for the platform-specific artifact to resolve
 */
fun DependencyHandlerScope.native(group: String, name: String, version: String, classifierFunction: (NativePlatforms) -> String) {
    forEachPlatform {
        add(it.platformName, "$group:$name:$version:${classifierFunction(it)}")
    }
}

fun DependencyHandlerScope.native(dep: ProjectDependency) {
    forEachPlatform {
        add(it.platformName, dep)
    }
}

fun DependencyHandlerScope.nativeProject(path: String) {
    forEachPlatform {
        nativeProject(path, it)
    }
}

fun DependencyHandlerScope.nativeProject(path: String, platform: NativePlatforms) {
    add(platform.platformName, project(path, platform.platformName))
    add("compile", project(path, "compile"))
    if (platform.platformName == currentPlatform) {
        add("compileOnly", project(path))
        add("runtime", project(path))
    }
}

fun forEachPlatform(action: (NativePlatforms) -> Unit) {
    NativePlatforms.values().forEach(action)
}