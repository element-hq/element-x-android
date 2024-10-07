/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.x.intent

import android.app.Activity
import androidx.compose.ui.platform.UriHandler
import io.element.android.libraries.androidutils.system.openUrlInExternalApp

class SafeUriHandler(private val activity: Activity) : UriHandler {
    override fun openUri(uri: String) {
        activity.openUrlInExternalApp(uri)
    }
}
