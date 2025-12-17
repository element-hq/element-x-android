/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.crypto.historyvisible

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class HistoryVisibleStateProvider : PreviewParameterProvider<HistoryVisibleState> {
    override val values: Sequence<HistoryVisibleState>
    get() = sequenceOf(
            aHistoryVisibleState(showAlert = true),
        )
}

internal fun aHistoryVisibleState(
    showAlert: Boolean = false,
    eventSink: (HistoryVisibleEvent) -> Unit = {},
) = HistoryVisibleState(
    showAlert,
    eventSink = eventSink,
)
