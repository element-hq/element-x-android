/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.tools

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class RecoveryKeyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(
                text.text
                    .chunked(4)
                    .joinToString(separator = " ")
            ),
            offsetMapping = RecoveryKeyOffsetMapping(text.text),
        )
    }

    class RecoveryKeyOffsetMapping(private val text: String) : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset == 0) return 0
            val numberOfChunks = offset / 4
            return if (offset == text.length && offset % 4 == 0) {
                offset + numberOfChunks - 1
            } else {
                offset + numberOfChunks
            }
        }

        override fun transformedToOriginal(offset: Int): Int {
            val numberOfChunks = offset / 5
            return offset - numberOfChunks
        }
    }
}
