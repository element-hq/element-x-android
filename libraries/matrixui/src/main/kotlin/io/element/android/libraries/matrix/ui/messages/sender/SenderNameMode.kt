/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.messages.sender

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
sealed interface SenderNameMode {
    data class Timeline(val mainColor: Color) : SenderNameMode
    data object Reply : SenderNameMode
    data object ActionList : SenderNameMode
}
