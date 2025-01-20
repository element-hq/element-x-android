/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.impl.previews

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.dateformatter.api.DateFormatterMode

class DateFormatterModeProvider : PreviewParameterProvider<DateFormatterMode> {
    override val values: Sequence<DateFormatterMode>
        get() = DateFormatterMode.entries.asSequence()
}
