/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
