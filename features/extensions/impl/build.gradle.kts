import extension.setupDependencyInjection
/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
plugins {
    id("io.element.android-compose-library")
}
android {
    namespace = "io.element.android.features.extensions.impl"
}
setupDependencyInjection()
dependencies {
    api(projects.features.extensions.api)
    implementation(projects.libraries.core)
    implementation(projects.libraries.architecture)
    implementation(projects.libraries.matrix.api)
    implementation(projects.libraries.designsystem)
    implementation(projects.libraries.uiStrings)
    implementation(libs.kotlinx.collections.immutable)
}
