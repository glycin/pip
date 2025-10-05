import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.2.0"
    id("com.gradleup.shadow") version "9.2.0"
}

val v = "1.33.7"
group = "com.glycin"
version = v

repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform  {
    pluginConfiguration {
        id = "pip"
        name = "P.I.P"
        version = v

        ideaVersion {
            sinceBuild = "232"
            untilBuild = provider { null }
        }

        vendor {
            name = "Glycin"
            url = "https://github.com/glycin"
        }
    }

    publishing {}

    signing{}
}

private val ktorVersion = "3.2.2"
private val logbackVersion = "1.5.18"

dependencies {
    intellijPlatform{
        intellijIdeaCommunity("2024.2.3")
        bundledPlugin("com.intellij.java")
        pluginVerifier()
        zipSigner()
        testFramework(TestFrameworkType.Platform)
    }

    implementation("io.ktor:ktor-client-cio:$ktorVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
    implementation("io.ktor:ktor-client-logging:$ktorVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion") {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
    implementation("io.ktor:ktor-serialization-gson:$ktorVersion"){
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

tasks.named("buildPlugin") {
    dependsOn(tasks.named("shadowJar"))
}

tasks.named("prepareSandbox") {
    dependsOn(tasks.named("shadowJar"))
}

// Replace the default jar with the shadow jar inside the plugin sandbox/zip
tasks.withType<org.jetbrains.intellij.platform.gradle.tasks.PrepareSandboxTask>().configureEach {
    // remove default unshaded jar
    doFirst {
        val libDir = destinationDir.resolve("${project.name}/lib")
        // delete any existing project jar(s)
        libDir.listFiles { f -> f.name.endsWith(".jar") && !f.name.contains("-all") }?.forEach { it.delete() }
        // copy shadow jar
        val shadow = tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar").get().archiveFile.get().asFile
        shadow.copyTo(libDir.resolve(shadow.name), overwrite = true)
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("shadow-all")

    // Relocate to isolate from IDE/platform
    relocate("io.ktor", "shadow.io.ktor")
    //relocate("kotlinx.coroutines", "shadow.kotlinx.coroutines")
    // Add more if needed (e.g., okhttp, guava), but avoid relocating kotlin.*
    // minimize may need excludes if something is used reflectively
    minimize()
}

kotlin{
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}