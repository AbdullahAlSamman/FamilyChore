package org.aals.family.chore.convention

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class RoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kmpExtension ->
                kmpExtension.sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.findLibrary("room.runtime").get())
                    implementation(libs.findLibrary("sqlite.bundled").get())
                }

                val isMac = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)
                val roomCompiler = libs.findLibrary("room.compiler").get()

                dependencies {
                    add("kspAndroid", roomCompiler)
                    add("kspJvm", roomCompiler)
                    if (isMac) {
                        add("kspIosArm64", roomCompiler)
                        add("kspIosSimulatorArm64", roomCompiler)
                    }
                }
            }
        }
    }
}
