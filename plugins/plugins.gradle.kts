subprojects {
    apply {
        plugin("com.github.johnrengelman.shadow")
        plugin("java-library")
    }
    dependencies {
        compile(project(":api"))
        compile(group = "edu.wpi.first.cscore", name = "cscore-java", version = "+")
        compile(group = "org.opencv", name = "opencv-java", version = "3.2.0")
        implementation(group = "edu.wpi.first.cscore", name = "cscore-jni", version = "+")
        implementation(group = "org.opencv", name = "opencv-jni", version = "3.2.0")
    }
}
