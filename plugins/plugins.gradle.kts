import edu.wpi.first.wpilib.versioning.ReleaseType
import org.gradle.jvm.tasks.Jar

// Ensure that the WPILibVersioningPlugin is setup by setting the release type, if releaseType wasn't
// already specified on the command line
if (!hasProperty("releaseType")) {
    WPILibVersion {
        releaseType = ReleaseType.DEV
    }
}

subprojects {
    plugins {
        id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "2.0"
        `java-library`
    }
    dependencies {
        compile(group = "com.google.code.findbugs", name = "annotations", version = "+")
        compile(project(":api"))
        compile(group = "edu.wpi.first.cscore", name = "cscore-java", version = "+")
        compile(group = "org.opencv", name = "opencv-java", version = "3.2.0")
        compile(group = "org.jcodec", name = "jcodec-javase", version = "0.2.0")
        implementation(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "+", classifier = "all")
        implementation(group = "org.opencv", name = "opencv-jni", version = "+", classifier = "all")
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
        create<MavenPublication>("api") {
            groupId = "edu.wpi.first.shuffleboard.plugin"
            artifactId = project.name
            getWPILibVersion()?.let { version = it }
            val jar: Jar by tasks
            artifact(jar)
            artifact(javadocJar)
            artifact(sourceJar)
        }
    }
}

/**
 * @return [edu.wpi.first.wpilib.versioning.WPILibVersioningPluginExtension.version] value or null
 * if that value is the empty string.
 */
fun getWPILibVersion(): String? = if (WPILibVersion.version != "") WPILibVersion.version else null
