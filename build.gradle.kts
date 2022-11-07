import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    application
}

group = "ru.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:1.33")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.fusesource.jansi:jansi:2.4.0")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.2")
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
}

application {
    mainClass.set("ru.vm.mpb.MainKt")
    applicationDefaultJvmArgs = listOf("-Djansi.colors=truecolor")
}