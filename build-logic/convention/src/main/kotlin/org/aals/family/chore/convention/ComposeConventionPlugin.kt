package org.aals.family.chore.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class ComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.compose")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kmpExtension ->
                kmpExtension.sourceSets.findByName("androidMain")?.dependencies {
                    implementation(libs.findLibrary("compose.uiToolingPreview").get())
                }
                kmpExtension.sourceSets.findByName("jvmMain")?.dependencies {
                    implementation(libs.findLibrary("compose.uiToolingPreview").get())
                }
            }
        }
    }
}
