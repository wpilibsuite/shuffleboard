description = """
The bundled CameraServer plugin. This plugin provides data sources and widgets for viewing MJPEG streams from the WPILib CameraServer.
""".trimIndent().trim()

val platform = ext["platform"] as String
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

fun javaCppClassifier(platform: String): String {
    return when (platform) {
        "win32" -> "windows-x86"
        "win64" -> "windows-x86_64"
        "mac64" -> "macosx-x86_64"
        "linux32" -> "linux-x86"
        "linux64" -> "linux-x86_64"
        else -> throw UnsupportedOperationException("JavaCPP does not support the '$platform' platform")
    }
}

fun wpilibClassifier(platform: String): String {
    return when (platform) {
        "win32" -> "windowsx86"
        "win64" -> "windowsx86-64"
        "mac64" -> "osxx86-64"
        // No 32-bit Linux support
        "linux64" -> "linuxx86-64"
        else -> throw UnsupportedOperationException("WPILib does not support the '$platform' platform")
    }
}
