import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * Generates a dependency on a JavaFX artifact. The artifact name is preprended with `"javafx-"`, so `name` should be
 * something like `"base"` instead of `"javafx-base"` or `"graphics"` vs `"javafx-graphics"`.  This generates a
 * platform-specific dependency, so this should only be used as a `compileOnly` dependency _except_ in the app project,
 * which wants the dependencies as `compile` or `runtime` dependencies so they can be included in the fatjar
 * distribution.
 */
fun DependencyHandler.javafx(name: String, platform: String, version: String = "11"): String {
        if (platform == "win32") {
                return "edu.wpi.first.openjfx:javafx-$name:$version:${javaFxClassifier(platform)}"
        } else {
                return "org.openjfx:javafx-$name:$version:${javaFxClassifier(platform)}"
        }
}
