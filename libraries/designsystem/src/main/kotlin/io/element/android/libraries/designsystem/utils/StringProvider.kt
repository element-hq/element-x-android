/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.utils

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class StringProvider(val strings: List<String>) : PreviewParameterProvider<String> {
    override val values: Sequence<String>
        get() = strings.asSequence()
}
