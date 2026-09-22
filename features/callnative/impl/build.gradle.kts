import extension.setupDependencyInjection
import extension.testCommonDependencies

/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-library")
}

android {
    namespace = "io.element.android.features.callnative.impl"
}

setupDependencyInjection()

// Temporary: `-PelementCallLocalVersion` points this at a locally published build of
// element-call-android, because the library has no remote repository yet. Goes with the mavenLocal
// block in settings.gradle.kts. Only the BOM carries a version; every other artifact follows it.
val elementCallVersion: String = providers.gradleProperty("elementCallLocalVersion")
    .getOrElse(libs.versions.element.call.get())

dependencies {
    api(projects.features.callnative.api)

    implementation(platform("io.element.android:element-call-bom:$elementCallVersion"))
    implementation(libs.element.call.api)
    implementation(libs.element.call.impl)

    implementation(projects.features.call.api)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.di)
    implementation(projects.libraries.featureflag.api)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.preferences.api)
    // For NotificationIdProvider: the ongoing-call notification id is shared with the WebView call's
    // foreground service, so that the two paths cannot leave two call notifications in the shade.
    implementation(projects.libraries.push.api)
    implementation(projects.services.appnavstate.api)
    implementation(libs.androidx.corektx)

    testCommonDependencies(libs)
    testImplementation(libs.element.call.test)
    testImplementation(projects.features.call.test)
    testImplementation(projects.libraries.featureflag.test)
    testImplementation(projects.libraries.matrix.test)
    testImplementation(projects.libraries.preferences.test)
    testImplementation(projects.services.appnavstate.test)
}
