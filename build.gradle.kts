import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    application
}

group = "ru.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    testImplementation(kotlin("test", "1.9.22"))
    testImplementation("io.mockk:mockk:1.13.8")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
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