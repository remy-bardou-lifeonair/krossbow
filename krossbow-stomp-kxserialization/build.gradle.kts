plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
}

description = "An extension of Krossbow STOMP client using Kotlinx Serialization for message conversions"

kotlin {
    jvm()
    js {
        nodejs()
        browser()
    }
    ios()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.krossbowStompCore)
                api(libs.kotlinx.serialization.core)
                compileOnly(libs.kotlinx.serialization.json)
            }
        }
    }
}
