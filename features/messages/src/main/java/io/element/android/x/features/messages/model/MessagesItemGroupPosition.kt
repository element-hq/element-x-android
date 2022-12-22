/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.messages.model

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

sealed interface MessagesItemGroupPosition {
    object First : MessagesItemGroupPosition
    object Middle : MessagesItemGroupPosition
    object Last : MessagesItemGroupPosition
    object None : MessagesItemGroupPosition

    fun isNew(): Boolean = when (this) {
        First, None -> true
        else -> false
    }
}

internal class MessagesItemGroupPositionProvider : PreviewParameterProvider<MessagesItemGroupPosition> {
    override val values = sequenceOf(
        MessagesItemGroupPosition.First,
        MessagesItemGroupPosition.Middle,
        MessagesItemGroupPosition.Last,
        MessagesItemGroupPosition.None,
    )
}
