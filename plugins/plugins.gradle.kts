subprojects {
    apply {
        plugin("com.github.johnrengelman.shadow")
        plugin("java-library")
    }
    dependencies {
        compile(project(":api"))
    }
}
