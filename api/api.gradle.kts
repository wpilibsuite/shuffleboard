plugins {
    `java-library`
}

description = """
Public API for writing plugins for shuffleboard.
""".trimMargin()

dependencies {
    api(group = "com.google.guava", name = "guava", version = "21.0")
    api(group = "org.fxmisc.easybind", name = "easybind", version = "1.0.3")
    api(group = "org.controlsfx", name = "controlsfx", version = "8.40.11")
    api(group = "de.codecentric.centerdevice", name = "javafxsvg", version = "1.2.1")
    api(group = "edu.wpi.first.ntcore", name = "ntcore-java", version = "3.1.7-20170808143930-12-gccfeab5")
    implementation(group = "edu.wpi.first.wpiutil", name = "wpiutil-java", version = "2.0.0-20170808143537-16-gf0cc5d9")
    implementation(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = "3.1.7-20170808143930-12-gccfeab5", classifier = "all")
}

if (project.hasProperty("testClasses")) {
    junitPlatform {
        filters {
            includeClassNamePattern(project.property("testClasses").toString())
        }
    }
}

/*
 * Allows you to run the UI tests in headless mode by calling gradle with the -Pheadless argument
 */
if (project.hasProperty("jenkinsBuild") || project.hasProperty("headless")) {
    println("Running UI Tests Headless")
    junitPlatform {
        filters {
            tags {
                /*
                 * A category for UI tests that cannot run in headless mode, ie work properly with real windows
                 * but not with the virtualized ones in headless mode.
                 */
                exclude("NonHeadlessTests")
            }
        }
    }
    tasks {
        "junitPlatformTest"(JavaExec::class) {
            jvmArgs = listOf(
                    "-Djava.awt.headless=true",
                    "-Dtestfx.robot=glass",
                    "-Dtestfx.headless=true",
                    "-Dprism.order=sw",
                    "-Dprism.text=t2k"
            )
        }
    }
}
