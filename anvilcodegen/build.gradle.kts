/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kapt)
}

dependencies {
    implementation(projects.anvilannotations)
    api(libs.anvil.compiler.api)
    implementation(libs.anvil.compiler.utils)
    implementation(libs.kotlinpoet)
    implementation(libs.dagger)
    compileOnly(libs.google.autoservice.annotations)
    kapt(libs.google.autoservice)
}
