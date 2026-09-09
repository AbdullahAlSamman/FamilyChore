plugins {
    id("familychore.kmp.library")
    id("familychore.compose")
    id("familychore.room")
    id("familychore.ktor")
    id("familychore.koin")
}

compose.resources {
    publicResClass = true
    packageOfResClass = "familychore.core.generated.resources"
}

android {
    namespace = "org.aals.family.chore.core"
}

val isMac = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            api(libs.compose.components.resources)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            api(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.auth)
            implementation(libs.datastore.preferences)
            implementation(libs.kermit)
            implementation(libs.okio)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.jmdns)
        }
        if (isMac) {
            iosMain.dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.assertk)
            implementation(libs.turbine)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)
            implementation(libs.okio.fakefilesystem)
        }
    }
}


