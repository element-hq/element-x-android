/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components.markdown

import android.text.SpannableString
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.element.android.libraries.core.extensions.orEmpty

@Stable
class StableCharSequence(initialText: CharSequence = "") {
    private var value by mutableStateOf<SpannableString>(SpannableString(initialText))
    private var needsDisplaying by mutableStateOf(false)

    fun update(newText: CharSequence?, needsDisplaying: Boolean) {
        value = SpannableString(newText.orEmpty())
        this.needsDisplaying = needsDisplaying
    }

    fun value(): CharSequence = value
    fun needsDisplaying(): Boolean = needsDisplaying

    override fun toString(): String {
        return "StableCharSequence(value='$value', needsDisplaying=$needsDisplaying)"
    }
}
