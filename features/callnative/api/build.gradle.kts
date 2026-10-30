/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

plugins {
    // NativeCallHost renders the call, so this module carries a composable.
    id("io.element.android-compose-library")
}

android {
    namespace = "io.element.android.features.callnative.api"
}

// Temporary: `-PelementCallLocalVersion` points this at a locally published build of
// element-call-android. Goes with the repositories in settings.gradle.kts.
val elementCallVersion: String = providers.gradleProperty("elementCallLocalVersion")
    .getOrElse(libs.versions.element.call.get())

dependencies {
    // api, not implementation: the RTC core's MatrixBackend is part of MatrixRtcBackendFactory's
    // signature, and CallData of NativeCallEntryPoint's. The core comes with element-call, which
    // exposes it as an api dependency.
    // api, so that consumers of this module inherit the versions the BOM pins - the artifacts
    // below are declared without one.
    api(platform("io.element.android:element-call-bom:$elementCallVersion"))
    api(libs.element.call.api)
    api(libs.element.call.impl)
    // ElementCallActivity.Host, which NativeCallScreenHost is.
    api(libs.element.call.ui)
    api(projects.features.call.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
}
