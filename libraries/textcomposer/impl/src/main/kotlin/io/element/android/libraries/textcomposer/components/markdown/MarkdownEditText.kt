/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components.markdown

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText

internal class MarkdownEditText(
    context: Context,
) : AppCompatEditText(context) {
    var onSelectionChangeListener: ((Int, Int) -> Unit)? = null

    private var isModifyingText = false

    fun updateEditableText(charSequence: CharSequence) {
        isModifyingText = true
        editableText.clear()
        editableText.append(charSequence)
        isModifyingText = false
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        isModifyingText = true
        super.setText(text, type)
        isModifyingText = false
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        if (!isModifyingText) {
            onSelectionChangeListener?.invoke(selStart, selEnd)
        }
    }
}
