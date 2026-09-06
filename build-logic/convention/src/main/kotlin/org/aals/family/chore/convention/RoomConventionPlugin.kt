package org.aals.family.chore.convention

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
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

            val roomCompiler = libs.findLibrary("room.compiler").get()
            val isMac = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)

            dependencies.add("kspAndroid", roomCompiler)
            dependencies.add("kspJvm", roomCompiler)
            if (isMac) {
                dependencies.add("kspIosArm64", roomCompiler)
                dependencies.add("kspIosSimulatorArm64", roomCompiler)
            }

            extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kmpExtension ->
                kmpExtension.sourceSets.getByName("commonMain").dependencies {
                    api(libs.findLibrary("room.runtime").get())
                    api(libs.findLibrary("sqlite.bundled").get())
                }
            }
        }
    }
}
