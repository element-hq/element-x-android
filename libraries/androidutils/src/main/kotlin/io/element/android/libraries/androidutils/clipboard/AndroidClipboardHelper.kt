/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.clipboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AndroidClipboardHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) : ClipboardHelper {
    private val clipboardManager = requireNotNull(context.getSystemService<ClipboardManager>())

    override fun copyPlainText(text: String) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("", text))
    }
}
