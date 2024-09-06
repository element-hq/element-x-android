/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class InvitesStateProvider : PreviewParameterProvider<InvitesState> {
    override val values: Sequence<InvitesState>
        get() = sequenceOf(
            InvitesState.SeenInvites,
            InvitesState.NewInvites,
        )
}
