package org.aals.family.chore.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            
            extensions.findByType(KotlinMultiplatformExtension::class.java)?.let { kmpExtension ->
                kmpExtension.sourceSets.getByName("commonMain").dependencies {
                    implementation(libs.findLibrary("koin.core").get())
                }
            }
        }
    }
}
