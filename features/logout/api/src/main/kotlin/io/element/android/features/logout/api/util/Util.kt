/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.api.util

import android.app.Activity
import io.element.android.libraries.androidutils.browser.openUrlInChromeCustomTab
import timber.log.Timber

fun onSuccessLogout(
    activity: Activity,
    darkTheme: Boolean,
    url: String?,
) {
    Timber.d("Success logout with result url: $url")
    url?.let {
        activity.openUrlInChromeCustomTab(null, darkTheme, it)
    }
}
