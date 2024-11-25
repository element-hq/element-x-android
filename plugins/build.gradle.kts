/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(libs.android.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.appdistribution.gradle)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.autonomousapps.dependencyanalysis.plugin)
    implementation(libs.anvil.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.compose.compiler.plugin)
}
