/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Ravel uses semantic versioning (MAJOR.MINOR.PATCH).
 * Update these values when cutting a release, or let the CI workflow inject them from the git tag.
 *
 * VERSION_CODE must always increase. We use: MAJOR * 10000 + MINOR * 100 + PATCH.
 * Note: versionCode is further multiplied by 10 in app/build.gradle.kts to encode ABI.
 */
private const val versionMajor = 0
private const val versionMinor = 1
private const val versionPatch = 1

object Versions {
    const val VERSION_CODE = versionMajor * 10_000 + versionMinor * 100 + versionPatch
    val VERSION_NAME = "$versionMajor.$versionMinor.$versionPatch"

    /**
     * Compile SDK version. Must be updated when a new Android version is released.
     * When updating COMPILE_SDK, please also update BUILD_TOOLS_VERSION.
     */
    const val COMPILE_SDK = 36

    /**
     * Build tools version. Must be kept in sync with COMPILE_SDK.
     * The value is used by the release script.
     */
    @Suppress("unused")
    private const val BUILD_TOOLS_VERSION = "36.0.0"

    /**
     * Target SDK version. Should be kept up to date with COMPILE_SDK.
     */
    const val TARGET_SDK = 36

    /**
     * Minimum SDK version for FOSS builds.
     */
    private const val MIN_SDK_FOSS = 24

    /**
     * Minimum SDK version for Enterprise builds.
     */
    private const val MIN_SDK_ENTERPRISE = 33

    /**
     * minSdkVersion that will be set in the Android Manifest.
     */
    val minSdk = if (isEnterpriseBuild) MIN_SDK_ENTERPRISE else MIN_SDK_FOSS

    /**
     * Java version used for compilation.
     * Update this value when you want to use a newer Java version.
     */
    private const val JAVA_VERSION = 21

    val javaVersion: JavaVersion = JavaVersion.toVersion(JAVA_VERSION)
    val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(JAVA_VERSION)

    // Sanity check
    init {
        require(BUILD_TOOLS_VERSION.startsWith(COMPILE_SDK.toString())) { "When updating COMPILE_SDK, please also update BUILD_TOOLS_VERSION" }
    }
}
