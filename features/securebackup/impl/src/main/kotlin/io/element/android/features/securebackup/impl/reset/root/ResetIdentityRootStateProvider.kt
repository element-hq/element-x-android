/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.reset.root

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class ResetIdentityRootStateProvider : PreviewParameterProvider<ResetIdentityRootState> {
    override val values: Sequence<ResetIdentityRootState>
        get() = sequenceOf(
            ResetIdentityRootState(
                displayConfirmationDialog = false,
                eventSink = {}
            ),
            ResetIdentityRootState(
                displayConfirmationDialog = true,
                eventSink = {}
            )
        )
}
