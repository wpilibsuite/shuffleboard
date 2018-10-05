description = """
The bundled NetworkTable plugin. This plugin provides sources for network tables (both single entries and arbitrary
subtables) and a widget for displaying MapData as a tree table.
""".trim()

dependencies {
    val ntcoreVersion = "2018.4.+"
    val wpiUtilVersion = "2018.4.+"

    compile(group = "edu.wpi.first.ntcore", name = "ntcore-java", version = ntcoreVersion)
    native(group = "edu.wpi.first.ntcore", name = "ntcore-jni", version = ntcoreVersion, classifierFunction = ::wpilibClassifier)
    compile(group = "edu.wpi.first.wpiutil", name = "wpiutil-java", version = wpiUtilVersion)
}
