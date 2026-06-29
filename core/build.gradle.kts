plugins {
    id("familychore.kmp.library")
    id("familychore.compose")
    id("familychore.room")
    id("familychore.ktor")
    id("familychore.koin")
}

kotlin {
    android {
        namespace = "org.aals.family.chore.core"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
        }
    }
}

dependencies {
    // Room KSP for all targets
    val roomCompiler = libs.room.compiler
    add("kspAndroid", roomCompiler)
    add("kspJvm", roomCompiler)
    add("kspIosArm64", roomCompiler)
    add("kspIosSimulatorArm64", roomCompiler)
}
