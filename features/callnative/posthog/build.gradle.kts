import extension.setupDependencyInjection

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
    namespace = "io.element.android.features.callnative.posthog"
}

setupDependencyInjection()

// Only added to the gplay flavor: F-Droid builds must not include PostHog, see https://github.com/element-hq/element-x-android/issues/7801
dependencies {
    implementation(projects.features.callnative.api)
    implementation(libs.posthog) {
        exclude("com.android.support", "support-annotations")
    }
    implementation(projects.libraries.core)
    implementation(projects.libraries.di)
    implementation(projects.libraries.matrix.api)
}
