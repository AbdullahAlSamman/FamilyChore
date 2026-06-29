package org.aals.family.chore.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KtorConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kmpExtension ->
                kmpExtension.sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.findLibrary("ktor.client.core").get())
                    implementation(libs.findLibrary("ktor.client.content.negotiation").get())
                    implementation(libs.findLibrary("ktor.serialization.kotlinx.json").get())
                    implementation(libs.findLibrary("ktor.client.logging").get())
                    implementation(libs.findLibrary("ktor.client.auth").get())
                }
            }
        }
    }
}
