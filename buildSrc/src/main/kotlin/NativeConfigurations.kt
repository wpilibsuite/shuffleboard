import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.dsl.DependencyHandler
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
 * Adds a dependency to the configuration for the given platform.
 */
fun DependencyHandler.add(platform: NativePlatforms, dependencyNotation: Any) = add(platform.platformName, dependencyNotation)

/**
 * Creates a [Configuration] for the given platform.  If the given platform is also the current platform (i.e. the
 * operating system running the Gradle build), then the `compileOnly`, `shadow`, and `testCompile` configurations
 * will extend from the native configuration for purposes of being able to run the application and tests.
 */
internal fun Project.nativeConfig(platform: NativePlatforms): Configuration {
    val configuration = configurations.create(platform.platformName)
    if (platform == currentPlatform) {
        configurations.getByName("compileOnly").extendsFrom(configuration)
        configurations.getByName("testImplementation").extendsFrom(configuration)
        configurations.getByName("shadow").extendsFrom(configuration)
    }
    return configuration
}

/**
 * Creates all the native configurations for the project.
 */
fun Project.createNativeConfigurations() = forEachPlatform { platform -> nativeConfig(platform) }

/**
 * Adds a dependency on a native (platform-specific) artifact.
 *
 * @param group              the group ID of the artifact
 * @param name               the name of the artifact
 * @param version            the version of the artifact (wildcards are supported)
 * @param classifierFunction a function that takes a native platform and returns the classifier
 *                           for the platform-specific artifact to resolve
 */
fun DependencyHandler.native(group: String, name: String, version: String, classifierFunction: (NativePlatforms) -> String) {
    forEachPlatform {
        add(it, "$group:$name:$version:${classifierFunction(it)}")
    }
}


fun DependencyHandler.nativeProject(path: String, type: String = "implementation") {
    add(type, project(path))
    forEachPlatform {
        this.nativeProject(type, path, it)
    }
}

internal fun DependencyHandler.nativeProject(type: String, path: String, platform: NativePlatforms) {
    add(platform, project(path, platform.platformName))
}

/**
 * Performs some action for all supported native platforms.
 */
fun forEachPlatform(action: (NativePlatforms) -> Unit) {
    NativePlatforms.values().forEach(action)
}