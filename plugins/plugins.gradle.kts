import edu.wpi.first.wpilib.versioning.ReleaseType
import org.gradle.jvm.tasks.Jar

subprojects {
    afterEvaluate {
        // Ensure that the WPILibVersioningPlugin is setup by setting the release type, if releaseType wasn't
        // already specified on the command line
        if (!hasProperty("releaseType")) {
            WPILibVersion {
                releaseType = ReleaseType.DEV
            }
        }
        apply(plugin = "java-library")
        dependencies {
            compileOnly(group = "com.google.code.findbugs", name = "annotations", version = "+")
            compile(project(":api"))
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
        publishing.publications {
            create<MavenPublication>("plugin.${project.name}") {
                groupId = "edu.wpi.first.shuffleboard.plugin"
                artifactId = project.name
                getWPILibVersion()?.let { version = it }
                from(components["java"])
                artifact(javadocJar)
                artifact(sourceJar)
            }
        }
    }
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
