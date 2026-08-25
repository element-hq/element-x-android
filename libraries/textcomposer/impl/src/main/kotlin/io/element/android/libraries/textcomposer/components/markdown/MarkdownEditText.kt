/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.components.markdown

import android.content.Context
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

internal class MarkdownEditText(
    context: Context,
) : AppCompatEditText(context) {
    var onSelectionChangeListener: ((Int, Int) -> Unit)? = null

    var onEnterKeyListener: (() -> Boolean)? = null

    private var isModifyingText = false

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_ENTER &&
            !event.isShiftPressed &&
            event.deviceId != KeyCharacterMap.VIRTUAL_KEYBOARD
        ) {
            val listener = onEnterKeyListener
            if (listener != null) {
                return when (event.action) {
                    KeyEvent.ACTION_UP -> true
                    KeyEvent.ACTION_DOWN -> if (event.repeatCount == 0) listener() else true
                    else -> super.dispatchKeyEvent(event)
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }

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

    // When using the EditText within a Compose layout, we need to override focusSearch to prevent the default behavior
    // Otherwise it can try searching for focusable nodes in the Compose hierarchy while they're being laid out, which will crash
    override fun focusSearch(direction: Int): View? {
        return null
    }
}
