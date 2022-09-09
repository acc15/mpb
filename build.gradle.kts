import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    application
}

group = "ru.vm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val jacksonVersion = "2.13.3"
val coroutinesVersion = "1.6.4"
dependencies {
    implementation("org.yaml:snakeyaml:1.31")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.slf4j:slf4j-nop:1.7.36")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClass.set("ru.vm.mpb.MainKt")
}