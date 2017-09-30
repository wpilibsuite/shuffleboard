description = """
The bundled CameraServer plugin. This plugin provides data sources and widgets for viewing MJPEG streams from the WPILib CameraServer.
""".trimIndent().trim()

plugins {
    id("edu.wpi.first.wpilib.versioning.WPILibVersioningPlugin") version "1.6"
}

dependencies {
    api(group = "edu.wpi.first.cscore", name = "cscore-java", version = "+")
    implementation(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "+")
    implementation(group = "edu.wpi.first.wpilib", name = "opencv", version = "3.2.0")
}
