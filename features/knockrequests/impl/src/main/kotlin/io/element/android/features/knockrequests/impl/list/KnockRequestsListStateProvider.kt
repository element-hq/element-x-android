/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class KnockRequestsListStateProvider : PreviewParameterProvider<KnockRequestsListState> {
    override val values: Sequence<KnockRequestsListState>
        get() = sequenceOf(
            aKnockRequestsListState(),
            // Add other states here
        )
}

fun aKnockRequestsListState() = KnockRequestsListState(
    eventSink = {}
)
