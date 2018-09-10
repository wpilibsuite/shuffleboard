description = """
The bundled CameraServer plugin. This plugin provides data sources and widgets for viewing MJPEG streams from the WPILib CameraServer.
""".trimIndent().trim()

val platform: String by extra
val javaCppClassifier = javaCppClassifier(platform)
val javaCppVersion = "1.4.1"
val wpilibClassifier = wpilibClassifier(platform)

dependencies {
    // ntcore, cscore dependencies
    compile(project(":plugins:networktables"))
    compile(group = "edu.wpi.first.cscore", name = "cscore-java", version = "+")
    runtime(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "+", classifier = wpilibClassifier)
    compile(group = "org.opencv", name = "opencv-java", version = "3.2.0")
    runtime(group = "org.opencv", name = "opencv-jni", version = "+", classifier = wpilibClassifier)

    // FFMPEG binaries
    compile(group = "org.bytedeco", name = "javacv", version = javaCppVersion)
    compile(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-$javaCppVersion", classifier = javaCppClassifier)
    compile(group = "org.bytedeco.javacpp-presets", name = "opencv", version = "3.4.1-$javaCppVersion", classifier = javaCppClassifier)
    testRuntime(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-$javaCppVersion", classifier = javaCppClassifier)
}
