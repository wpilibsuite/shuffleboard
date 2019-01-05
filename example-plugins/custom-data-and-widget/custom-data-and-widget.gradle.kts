plugins {
    `java-library`
}

repositories {
    mavenCentral()
    maven {
        name = "WPILib"
        setUrl("http://first.wpi.edu/FRC/roborio/maven/release")
    }
}

dependencies {
    compileOnly(group = "edu.wpi.first.shuffleboard", name = "api", version = "2019.1.1")
}

tasks.register<Copy>("installPlugin") {
    from(tasks.named("jar"))
    into("${System.getProperty("user.home")}/Shuffleboard/plugins")
    description = "Builds the plugin JAR and installs it in the Shuffleboard plugins directory."
    group = "Shuffleboard Plugin"
}
