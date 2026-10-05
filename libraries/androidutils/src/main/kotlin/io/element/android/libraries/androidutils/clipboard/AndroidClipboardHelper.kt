/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.clipboard

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.core.content.getSystemService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.di.annotations.ApplicationContext

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AndroidClipboardHelper(
    @ApplicationContext private val context: Context,
) : ClipboardHelper {
    private val clipboardManager = requireNotNull(context.getSystemService<ClipboardManager>())

    override fun copyPlainText(
        text: String,
        isSensitive: Boolean,
        onSuccessOnOldDevice: () -> Unit,
    ) {
        val clipData = ClipData.newPlainText("", text)
        if (isSensitive) {
            clipData.description.extras = PersistableBundle().apply {
                putBoolean(EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboardManager.setPrimaryClip(clipData)
        // On Android 13+, the system already displays a confirmation when content is copied to the clipboard.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            onSuccessOnOldDevice()
        }
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
