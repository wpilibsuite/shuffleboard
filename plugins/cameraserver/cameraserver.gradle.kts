description = """
The bundled CameraServer plugin. This plugin provides data sources and widgets for viewing MJPEG streams from the WPILib CameraServer.
""".trimIndent().trim()

dependencies {
    compile(group = "edu.wpi.first.cscore", name = "cscore-java", version = "+")
    compile(group = "org.opencv", name = "opencv-java", version = "3.2.0")
    compile(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "+", classifier = "all")
    compile(group = "org.opencv", name = "opencv-jni", version = "+", classifier = "all")
    compile(group = "org.bytedeco", name = "javacv", version = "1.4.1")

    // FFMPEG binaries
    runtime(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-1.4.1", classifier = "linux-x86_64")
    runtime(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-1.4.1", classifier = "windows-x86_64")
    runtime(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-1.4.1", classifier = "windows-x86")
    runtime(group = "org.bytedeco.javacpp-presets", name = "ffmpeg", version = "3.4.2-1.4.1", classifier = "macosx-x86_64")
}
