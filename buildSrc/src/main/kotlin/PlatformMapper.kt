/**
 * The platform performing the build. This is in the format `"<os.name><os.arch>"`, e.g. `win32`, `mac64`, `linux64`.
 */
val currentPlatform: String by lazy {
    val osName = System.getProperty("os.name")
    val os: String = when {
        osName.contains("windows", true) -> "win"
        osName.contains("mac", true) -> "mac"
        osName.contains("linux", true) -> "linux"
        else -> throw UnsupportedOperationException("Unknown OS: $osName")
    }
    val osArch = System.getProperty("os.arch")
    val arch: String =
            if (osArch.contains("x86_64") || osArch.contains("amd64")) {
                "64"
            } else if (osArch.contains("x86")) {
                "32"
            } else {
                throw UnsupportedOperationException(osArch)
            }
    os + arch
}

/**
 * Generates a classifier string for a platform-specific WPILib native library.
 *
 * @param platform the platform string to get the WPILib classifier for. Platform strings should be one of:
 * - win32
 * - win64
 * - mac64
 * - linux32
 * - linux64
 * Note that WPILib typically only has builds for win32, win64, mac64, and linux64
 */
fun wpilibClassifier(platform: String) = when (platform) {
    "win32" -> "windowsx86"
    "win64" -> "windowsx86-64"
    "mac64" -> "osxx86-64"
    // No 32-bit Linux support
    "linux64" -> "linuxx86-64"
    else -> throw UnsupportedOperationException("WPILib does not support the '$platform' platform")
}


/**
 * Generates a classifier string for a platform-specific JavaCPP native library.
 *
 * @param platform the platform string to get the WPILib classifier for. Platform strings should be one of:
 * - win32
 * - win64
 * - mac64
 * - linux32
 * - linux64
 */
fun javaCppClassifier(platform: String): String = when (platform) {
    "win32" -> "windows-x86"
    "win64" -> "windows-x86_64"
    "mac64" -> "macosx-x86_64"
    "linux32" -> "linux-x86"
    "linux64" -> "linux-x86_64"
    else -> throw UnsupportedOperationException("JavaCPP does not support the '$platform' platform")
}


/**
 * Generates a classifier string for a platform-specific JavaFX artifact.
 *
 * @param platform the platform string to get the WPILib classifier for. Platform strings should be one of:
 * - win32
 * - win64
 * - mac64
 * - linux64
 */
fun javaFxClassifier(platform: String): String = when (platform) {
    "win32" -> "win32"
    "win64" -> "win"
    "mac64" -> "mac"
    "linux64" -> "linux"
    else -> throw UnsupportedOperationException("JavaFX does not support the '$platform' platform")
}
