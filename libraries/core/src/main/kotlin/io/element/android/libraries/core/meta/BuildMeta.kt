/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.core.meta

data class BuildMeta(
    val buildType: BuildType,
    val isDebuggable: Boolean,
    val applicationName: String,
    val productionApplicationName: String,
    val desktopApplicationName: String,
    val applicationId: String,
    val isEnterpriseBuild: Boolean,
    val lowPrivacyLoggingEnabled: Boolean,
    val versionName: String,
    val versionCode: Long,
    val gitRevision: String,
    val gitBranchName: String,
    val flavorDescription: String,
    val flavorShortDescription: String,
)
