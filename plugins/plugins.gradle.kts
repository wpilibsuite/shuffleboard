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
        plugins {
            id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "2.0"
            `java-library`
        }
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
        val checkForVersionUpdate = task("checkPluginVersionUpdated") {
            description = "Check that the plugin's version number has been updated"
            doLast {
                val changedFiles = getChangedFiles(project)
                if (changedFiles.isNotEmpty()) {
                    val pluginFileChanged = changedFiles.any { file ->
                        file.endsWith(suffix = "/${project.name}plugin.java", ignoreCase = true)
                    }
                    if (!pluginFileChanged) {
                        throw VersionNotUpdatedException("Plugin version was not updated for plugin '${project.name}'")
                    }
                    val pluginFile = project.java.sourceSets["main"].allJava.toList()
                            .first { file -> file.nameWithoutExtension.toLowerCase() == "${project.name}plugin" }
                    val versionRegex = Regex("^.*version\\s*=\\s*\"(.+)\".*$")
                    val versionLine = pluginFile.readLines()
                            .first { line -> line.matches(versionRegex) }
                    val version = versionRegex.matchEntire(versionLine)!!.groupValues[1]
                    val masterVersion = getMasterVersion(rootProject, pluginFile)
                    if (masterVersion.contains("version = \"$version\"")) {
                        throw VersionNotUpdatedException("Plugin version was not updated for plugin '${project.name}'")
                    }
                }
            }
        }

        tasks.getByName("check").dependsOn(checkForVersionUpdate)
    }
}

/**
 * Gets a list of the names of the files in the project that have changed on the current branch, including uncommitted
 * and unstaged files, relative to the `master` branch.
 *
 * @param project the project to get the changed files for
 */
fun getChangedFiles(project: Project): List<String> {
    val diffProc = Runtime.getRuntime().exec("git diff master --name-only")
    val input = diffProc.inputStream.bufferedReader().use { it.readText() }
    return input.split('\n')
            .filter { path -> path.startsWith("plugins/${project.name}") }
            .toList()
}

/**
 * Gets the version of a file on the `master` branch.
 */
fun getMasterVersion(rootProject: Project, file: File): String {
    val cmd = "git show master:${file.relativeTo(rootProject.projectDir)}"
    val lineHistProc = Runtime.getRuntime().exec(cmd)
    return lineHistProc.inputStream.bufferedReader().use { it.readText() }
}

class VersionNotUpdatedException(message: String) : GradleException(message)

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
