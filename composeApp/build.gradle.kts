import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val javafxVersion = "17.0.10"
val javafxPlatform = run {
    val osName = System.getProperty("os.name").lowercase()
    val osArch = System.getProperty("os.arch").lowercase()
    when {
        osName.contains("win") -> "win"
        osName.contains("mac") && osArch.contains("aarch64") -> "mac-aarch64"
        osName.contains("mac") -> "mac"
        osArch.contains("aarch64") -> "linux-aarch64"
        else -> "linux"
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.3.0"
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)

            // Lifecycle for Desktop - temporarily disabled due to Android dependencies
            // implementation(libs.androidx.lifecycle.viewmodelCompose) {
            //     exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-android")
            //     exclude(group = "androidx.lifecycle", module = "lifecycle-runtime-android")
            // }
            // implementation(libs.androidx.lifecycle.runtimeCompose) {
            //     exclude(group = "androidx.lifecycle", module = "lifecycle-runtime-android")
            // }

            // Networking & Serialization
            implementation("io.ktor:ktor-client-core:2.3.7")
            implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
            implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
            implementation("io.ktor:ktor-client-logging:2.3.7")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

            // Image Loading - Coil 3 for Compose Multiplatform
            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha10")
            implementation("io.coil-kt.coil3:coil-network-ktor2:3.0.0-alpha10")

            // Local Storage
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")

            // UUID Generation
            implementation("com.benasher44:uuid:0.8.2")

            // Logging
            implementation("ch.qos.logback:logback-classic:1.4.14")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.5.0")
            implementation("com.google.zxing:core:3.5.3")
            implementation("org.openjfx:javafx-base:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-controls:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-media:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-web:$javafxVersion:$javafxPlatform")
            implementation("org.openjfx:javafx-swing:$javafxVersion:$javafxPlatform")

            // JVM-specific implementations
            implementation("io.ktor:ktor-client-cio:2.3.7")
            implementation("app.cash.sqldelight:sqlite-driver:2.0.1")
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}

// Fix dependency conflicts
configurations.all {
    resolutionStrategy {
        eachDependency {
            if (requested.group == "androidx.compose.foundation" && requested.name == "foundation") {
                useVersion("1.7.3") // Use a compatible version
                because("Fix version conflict with Compose Multiplatform")
            }
            if (requested.group == "androidx.compose.ui" && requested.name == "ui") {
                useVersion("1.7.3") // Use a compatible version
                because("Fix version conflict with Compose Multiplatform")
            }
        }
    }
}
