description = """
The bundled CameraServer plugin. This plugin provides data sources and widgets for viewing MJPEG streams from the WPILib CameraServer.
""".trimIndent().trim()

val javaCppVersion = "1.4.1"

dependencies {
    // ntcore, cscore dependencies
    nativeProject(":plugins:networktables")

    compile(group = "edu.wpi.first.cscore", name = "cscore-java", version = "2020.+")
    native(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "2020.+", classifierFunction = ::wpilibClassifier)

    // FFMPEG binaries
    compile(group = "org.bytedeco", name = "javacv", version = javaCppVersion)
    native(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-$javaCppVersion", classifierFunction = ::javaCppClassifier)
    native(group = "org.bytedeco.javacpp-presets", name = "opencv", version = "3.4.1-$javaCppVersion", classifierFunction = ::javaCppClassifier)
}
