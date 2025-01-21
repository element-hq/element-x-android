/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class PictureInPictureStateProvider : PreviewParameterProvider<PictureInPictureState> {
    override val values: Sequence<PictureInPictureState>
        get() = sequenceOf(
            aPictureInPictureState(supportPip = true),
            aPictureInPictureState(supportPip = true, isInPictureInPicture = true),
        )
}

fun aPictureInPictureState(
    supportPip: Boolean = false,
    isInPictureInPicture: Boolean = false,
    eventSink: (PictureInPictureEvents) -> Unit = {},
): PictureInPictureState {
    return PictureInPictureState(
        supportPip = supportPip,
        isInPictureInPicture = isInPictureInPicture,
        eventSink = eventSink,
    )
}
