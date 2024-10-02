/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */
plugins {
    id("java-library")
    id("com.android.lint")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = Versions.javaCompileVersion
    targetCompatibility = Versions.javaCompileVersion
}

dependencies {
    implementation(libs.coroutines.core)
    testImplementation(libs.test.junit)
    testImplementation(libs.test.truth)
}
