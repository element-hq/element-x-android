import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    id("io.element.android-library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "io.element.android.libraries.matrix.impl"
}

setupDependencyInjection()

// Temporary: `-PelementCallLocalVersion` points this at a locally published build of
// element-call-android, because the library has no remote repository yet. Goes with the mavenLocal
// block in settings.gradle.kts. Only the BOM carries a version; every other artifact follows it.
val elementCallVersion: String = providers.gradleProperty("elementCallLocalVersion")
    .getOrElse(libs.versions.element.call.get())

val usesLocalRustSdk = file("${rootDir.path}/libraries/rustsdk/matrix-rust-sdk.aar").exists()

if (usesLocalRustSdk) {
    // element-call-matrix declares the Rust SDK as an ordinary Maven dependency, so with the local
    // AAR in place a debug classpath would carry two copies of org.matrix.rustcomponents.sdk.
    // Point the library's dependency at the same local AAR the app is using.
    configurations.matching { it.name.startsWith("debug") }.configureEach {
        resolutionStrategy.dependencySubstitution {
            substitute(module("org.matrix.rustcomponents:sdk-android"))
                .using(project(":libraries:rustsdk"))
                .because("the local Rust SDK AAR replaces the published one on debug builds")
        }
    }
}

dependencies {
    releaseImplementation(libs.matrix.sdk)
    if (usesLocalRustSdk) {
        println(
            "\nNote: Using local binary of the Rust SDK." +
                "\n      element-call-matrix is substituted onto it as well, so the native call runs" +
                "\n      against this SDK rather than the one it was published against.\n"
        )
        debugImplementation(projects.libraries.rustsdk)
    } else {
        debugImplementation(libs.matrix.sdk)
    }

    // The native call's Matrix transport. Only this module may hand it a raw SDK client.
    implementation(platform("io.element.android:element-call-bom:$elementCallVersion"))
    implementation(libs.element.call.api)
    implementation(projects.features.callnative.api)
    implementation(libs.element.call.matrix)
    implementation(projects.libraries.rustlsTls)

    implementation(projects.appconfig)
    implementation(projects.features.enterprise.api)
    implementation(projects.libraries.androidutils)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.network)
    implementation(projects.libraries.preferences.api)
    implementation(projects.libraries.sessionStorage.api)
    implementation(projects.libraries.workmanager.api)
    implementation(projects.services.analytics.api)
    implementation(projects.services.toolbox.api)
    api(projects.libraries.matrix.api)
    implementation(projects.libraries.core)
    implementation(variantOf(libs.jna) { artifactType("aar") })
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.serialization.json)
    implementation(libs.kotlinx.collections.immutable)

    testCommonDependencies(libs)
    testImplementation(projects.features.enterprise.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.libraries.previewutils)
    testImplementation(projects.libraries.sessionStorage.test)
    testImplementation(projects.libraries.workmanager.test)
    testImplementation(projects.services.analytics.test)
    testImplementation(projects.services.toolbox.test)
}
