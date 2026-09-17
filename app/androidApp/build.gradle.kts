import com.android.build.api.dsl.ApplicationExtension

plugins {
    id("familychore.android.application")
    id("familychore.compose")
}

dependencies {
    implementation(projects.app.shared)
    implementation(projects.core)
    implementation(projects.feature.auth)

    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    implementation(libs.kermit)

    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

extensions.configure<ApplicationExtension> {
    namespace = "org.aals.family.chore"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.aals.family.chore"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
