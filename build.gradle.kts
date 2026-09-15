import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.0"
}

group = "me.dalakoti07"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("io.github.hoc081098:FlowExt:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.10")
    implementation(kotlin("stdlib-jdk8"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
// Run any main() by class name:
//   ./gradlew runMain -PmainClass=coroutines.marcin.LimitedParallelismExKt --args="1"
tasks.register<JavaExec>("runMain") {
    group = "application"
    mainClass.set(project.findProperty("mainClass")?.toString() ?: "")
    classpath = sourceSets["main"].runtimeClasspath
}
