/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

import org.gradle.api.JavaVersion
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Version codes are quite sensitive, because there is a mix between bundle and APKs.
 * Max versionCode allowed by the PlayStore (for information):
 * 2_100_000_000
 *
 * Also note that the versionCode is multiplied by 10 in app/build.gradle.kts#L168:
 * ```
 * output.versionCode.set((output.versionCode.get() ?: 0) * 10 + abiCode))
 * ```
 * We are using a CalVer-like approach to version the application. The version code is calculated as follows:
 * - 2 digits for the year
 * - 2 digits for the month
 * - 1 (or 2) digits for the release number
 * Note that the version codes need to be greater than the ones calculated for the previous releases, so we use
 * year on 4 digits for this internal value.
 * So for instance, the first release of Jan 2025 will have:
 * - the version name: 25.01.0
 * - the version code: 20250100a (202_501_00a) where `a` stands for the architecture code
 */

private const val versionYear = 25
private const val versionMonth = 6

// Note: must be in [0,99]
private const val versionReleaseNumber = 2

object Versions {
    const val VERSION_CODE = (2000 + versionYear) * 10_000 + versionMonth * 100 + versionReleaseNumber
    val VERSION_NAME = "$versionYear.${versionMonth.toString().padStart(2, '0')}.$versionReleaseNumber"

    const val COMPILE_SDK = 35
    const val TARGET_SDK = 35

    // When updating the `minSdk`, make sure to update the value of `minSdkVersion` in the file `tools/release/release.sh`
    val minSdk = if (isEnterpriseBuild) 26 else 24

    private const val JAVA_VERSION = 21
    val javaVersion: JavaVersion = JavaVersion.toVersion(JAVA_VERSION)
    val javaLanguageVersion: JavaLanguageVersion = JavaLanguageVersion.of(JAVA_VERSION)
}
