/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class InitialsColorIntProvider : PreviewParameterProvider<Int> {
    override val values: Sequence<Int>
        get() = sequenceOf(0, 1, 2, 3, 4, 5, 6, 7)
}
