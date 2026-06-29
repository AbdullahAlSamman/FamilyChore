import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "org.aals.family.chore.convention"

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    implementation(libs.room.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.kotlin.serialization.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "familychore.android.application"
            implementationClass = "org.aals.family.chore.convention.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "familychore.android.library"
            implementationClass = "org.aals.family.chore.convention.AndroidLibraryConventionPlugin"
        }
        register("kmpLibrary") {
            id = "familychore.kmp.library"
            implementationClass = "org.aals.family.chore.convention.KmpLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "familychore.android.feature"
            implementationClass = "org.aals.family.chore.convention.AndroidFeatureConventionPlugin"
        }
        register("compose") {
            id = "familychore.compose"
            implementationClass = "org.aals.family.chore.convention.ComposeConventionPlugin"
        }
        register("koin") {
            id = "familychore.koin"
            implementationClass = "org.aals.family.chore.convention.KoinConventionPlugin"
        }
        register("room") {
            id = "familychore.room"
            implementationClass = "org.aals.family.chore.convention.RoomConventionPlugin"
        }
        register("ktor") {
            id = "familychore.ktor"
            implementationClass = "org.aals.family.chore.convention.KtorConventionPlugin"
        }
    }
}
