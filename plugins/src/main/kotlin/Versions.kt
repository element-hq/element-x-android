/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Version codes are quite sensitive, because there is a mix between bundle and APKs, and we have to take into
 * account the future upgrade of Element Android.
 * Max versionCode allowed by the PlayStore (for information):
 * 2_100_000_000
 * Current version code of EAx on the PlayStore, for the first uploaded beta (we cannot go below):
 * ----1_001_000
 * Current version code of EAx on the nightly:
 * ----1_001_000
 * Current version of Element Android (at some point EAx will replace this app) (v1.6.3)
 * ----40_106_03a where a stands for the architecture: 1, 2, 3, 4 and 0 for the universal APK
 * Current version of EAx distributed with Firebase app distribution:
 * ----1_002_000
 * Latest version of EAx distributed with Firebase app distribution (downgrading, so that's a problem)
 * -------10_200
 * Version when running the current debug build
 * -------10_200
 *
 * So adding 4_000_000 to the current version Code computed here should be fine, and since the versionCode
 * is multiplied by 10 in app/build.gradle.kts#L168:
 * ```
 * output.versionCode.set((output.versionCode.get() ?: 0) * 10 + abiCode))
 * ```
 * we will have:
 * Release version:
 * ---40_001_020
 * Nightly version:
 * ---40_001_020
 * Debug version:
 * ---40_010_200
 */

// Note: 2 digits max for each value
private const val versionMajor = 0
private const val versionMinor = 6

// Note: even values are reserved for regular release, odd values for hotfix release.
// When creating a hotfix, you should decrease the value, since the current value
// is the value for the next regular release.
private const val versionPatch = 6

object Versions {
    val versionCode = 4_000_000 + versionMajor * 1_00_00 + versionMinor * 1_00 + versionPatch
    val versionName = "$versionMajor.$versionMinor.$versionPatch"
    const val compileSdk = 34
    const val targetSdk = 34

    // When updating the `minSdk`, make sure to update the value of `minSdkVersion` in the file `tools/release/release.sh`
    val minSdk = if (isEnterpriseBuild) 26 else 24

    private const val JAVA_VERSION = 21
    val javaVersion: JavaVersion = JavaVersion.toVersion(JAVA_VERSION)
    val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(JAVA_VERSION)
}
