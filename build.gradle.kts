import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "ru.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.yaml:snakeyaml:2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.fusesource.jansi:jansi:2.4.1")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.11")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
        jvmTarget.set(JvmTarget.JVM_21)
    }
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