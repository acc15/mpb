import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.21"
    application
}

group = "ru.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.5")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.installDist {
    val dir = project.properties["dir"]
    if (dir != null) {
        destinationDir = File(dir.toString())
    }

    val configFile = "mpb.yaml"
    if (destinationDir.resolve(configFile).exists()) {
        exclude(configFile)
    }
    preserve {
        include(configFile)
    }
}

application {
    mainClass.set("ru.vm.mpb.MainKt")
    applicationDefaultJvmArgs = listOf("-Djansi.colors=truecolor")
}