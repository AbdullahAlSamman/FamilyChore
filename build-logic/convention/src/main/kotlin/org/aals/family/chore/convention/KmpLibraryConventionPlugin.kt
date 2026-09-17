package org.aals.family.chore.convention

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.kotlin.multiplatform.library")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            val compileSdkVal = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
            val minSdkVal = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
            val isMac = System.getProperty("os.name").contains("Mac")

            extensions.configure(KotlinMultiplatformExtension::class.java) {
                targets.configureEach {
                    if (this is KotlinMultiplatformAndroidLibraryTarget) {
                        compileSdk = compileSdkVal
                        minSdk = minSdkVal
                        androidResources {
                            enable = true
                        }
                        compilerOptions {
                            jvmTarget.set(JvmTarget.JVM_21)
                        }
                    }
                }

                jvm()
                if (isMac) {
                    val iosArm = iosArm64()
                    val iosSim = iosSimulatorArm64()
                    listOf(iosArm, iosSim).forEach { target ->
                        target.binaries.framework {
                            baseName = "Shared"
                            isStatic = true
                        }
                    }
                }
            }
        }
    }
}
