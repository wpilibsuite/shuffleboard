description = """
The bundled CameraServer plugin. This plugin provides data sources and widgets for viewing MJPEG streams from the WPILib CameraServer.
""".trimIndent().trim()

dependencies {
    compile(group = "edu.wpi.first.cscore", name = "cscore-java", version = "+")
    compile(group = "org.opencv", name = "opencv-java", version = "3.2.0")
    compile(group = "org.jcodec", name = "jcodec-javase", version = "0.2.0")
    compile(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "+", classifier = "all")
    compile(group = "org.opencv", name = "opencv-jni", version = "+", classifier = "all")
}
