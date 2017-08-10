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
    api(group = "edu.wpi.first.ntcore", name = "ntcore-java", version = "3.1.7-20170808143930-12-gccfeab5")
    implementation(group = "edu.wpi.first.wpiutil", name = "wpiutil-java", version = "2.0.0-20170808143537-16-gf0cc5d9")
    runtime(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = "3.1.7-20170808143930-12-gccfeab5", classifier = "all")
    testRuntime(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = "3.1.7-20170808143930-12-gccfeab5", classifier = "all")
}
