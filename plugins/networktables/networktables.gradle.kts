description = """
The bundled NetworkTable plugin. This plugin provides sources for network tables (both single entries and arbitrary
subtables) and a widget for displaying MapData as a tree table.
""".trim()

val platform = ext["platform"] as String
val wpilibClassifier = wpilibClassifier(platform)

dependencies {
    val ntcoreVersion = "2018.4.+"
    val wpiUtilVersion = "2018.4.+"

    compile(group = "edu.wpi.first.ntcore", name = "ntcore-java", version = ntcoreVersion)
    runtime(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = ntcoreVersion, classifier = wpilibClassifier)
    runtime(group = "edu.wpi.first.wpiutil", name = "wpiutil-java", version = wpiUtilVersion)
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
