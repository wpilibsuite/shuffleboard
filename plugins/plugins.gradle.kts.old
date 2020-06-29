import org.gradle.jvm.tasks.Jar
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import groovy.lang.GroovyObject

subprojects {
    afterEvaluate {
        apply(plugin = "java-library")
        dependencies {
            compileOnly(group = "com.google.code.findbugs", name = "annotations", version = "+")
            compile(project(":api"))
        }
        val sourceJar = task<Jar>("sourceJar") {
            description = "Creates a JAR that contains the source code."
            from(project.sourceSets["main"].allSource)
            classifier = "sources"
        }
        val javadocJar = task<Jar>("javadocJar") {
            dependsOn("javadoc")
            description = "Creates a JAR that contains the javadocs."
            from(tasks.named("javadoc"))
            classifier = "javadoc"
        }
        if (System.getenv()["RUN_AZURE_ARTIFACTORY_RELEASE"] != null) {
            artifactory {
                publish(delegateClosureOf<PublisherConfig> {
                    defaults(delegateClosureOf<GroovyObject> {
                        invokeMethod("publications", "plugin.${project.name}")
                    })
                })
            }
        }
        publishing.publications {
            register<MavenPublication>("plugin.${project.name}") {
                groupId = "edu.wpi.first.shuffleboard.plugin"
                artifactId = project.name
                version = project.version as String
                from(components["java"])
                artifact(javadocJar)
                artifact(sourceJar)
            }
        }
    }
}
