plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

description = "Test utilities for Krossbow WebSocket adapter implementations."

val serializationVersion = "1.2.0"

kotlin {
    jvm()
    js {
        useCommonJs()
        nodejs()
        browser()
    }
    ios()

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        }
        val commonMain by getting {
            dependencies {
                api(projects.krossbowWebsocketCore)
                api(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.core)
                implementation(libs.kotlinx.serialization.json)

                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                // for the test server
                implementation("com.pusher:java-websocket:1.4.1")

                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}
