/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ptt.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PttPrototypeStateProvider : PreviewParameterProvider<PttPrototypeState> {
    override val values: Sequence<PttPrototypeState>
        get() = sequenceOf(
            aPttPrototypeState(),
            aPttPrototypeState(isPttEnabled = true),
            aPttPrototypeState(isPttEnabled = true, hasLiveChannel = true, participantCount = 3),
            aPttPrototypeState(isPttEnabled = true, hasLiveChannel = true, participantCount = 4, isUserInChannel = true),
            aPttPrototypeState(isPttAvailable = false),
        )
}

fun aPttPrototypeState(
    isPttAvailable: Boolean = true,
    isPttEnabled: Boolean = false,
    hasLiveChannel: Boolean = false,
    participantCount: Int = 0,
    isUserInChannel: Boolean = false,
    eventSink: (PttPrototypeEvent) -> Unit = {},
) = PttPrototypeState(
    isPttAvailable = isPttAvailable,
    isPttEnabled = isPttEnabled,
    hasLiveChannel = hasLiveChannel,
    participantCount = participantCount,
    isUserInChannel = isUserInChannel,
    eventSink = eventSink,
)
