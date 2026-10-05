/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.system

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.core.content.getSystemService

class CopyToClipboardUseCase(
    private val context: Context,
) {
    /**
     * @param isSensitive if true, the clip is flagged so the system clipboard preview (Android 13+) and keyboards hide its content.
     */
    fun execute(text: CharSequence, isSensitive: Boolean = false) {
        val clipData = ClipData.newPlainText("", text)
        if (isSensitive) {
            clipData.description.extras = PersistableBundle().apply {
                putBoolean(EXTRA_IS_SENSITIVE, true)
            }
        }
        context.getSystemService<ClipboardManager>()?.setPrimaryClip(clipData)
    }

    private companion object {
        // The constant only exists from API 33, but the documentation recommends using its value on older versions too.
        val EXTRA_IS_SENSITIVE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ClipDescription.EXTRA_IS_SENSITIVE
        } else {
            "android.content.extra.IS_SENSITIVE"
        }
    }
}
