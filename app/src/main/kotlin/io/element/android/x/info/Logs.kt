/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.info

import android.content.Context
import io.element.android.libraries.androidutils.system.getVersionCodeFromManifest
import io.element.android.libraries.core.log.logger.wrapInBox
import io.element.android.x.BuildConfig
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun logApplicationInfo(context: Context, sdkGitSha: String) {
    val appVersion = "${BuildConfig.VERSION_NAME} (${context.getVersionCodeFromManifest()}) - ${BuildConfig.BUILD_TYPE} / ${BuildConfig.FLAVOR}"
    val date = SimpleDateFormat("MM-dd HH:mm:ss.SSSZ", Locale.US).format(Date())
    listOf(
        " Application version: $appVersion",
        " Element X: https://github.com/element-hq/element-x-android/commit/${BuildConfig.GIT_REVISION}",
        " SDK      : https://github.com/matrix-org/matrix-rust-sdk/commit/$sdkGitSha",
        " Local time: $date",
    )
        .wrapInBox(minBoxInsideWidth = 80)
        .forEach { Timber.d(it) }
}
