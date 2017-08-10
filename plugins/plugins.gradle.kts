subprojects {
    apply {
        plugin("com.github.johnrengelman.shadow")
        plugin("java-library")
    }
    dependencies {
        compile(project(":api"))
        fun testFx(name: String, version: String = "4.0.+") =
                create(group = "org.testfx", name = name, version = version)
        testCompile(testFx(name = "testfx-core"))
        testCompile(testFx(name = "testfx-junit5"))
        testRuntime(testFx(name = "openjfx-monocle", version = "8u76-b04"))
    }
}
