/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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

    override fun copyPlainText(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
    }
}
