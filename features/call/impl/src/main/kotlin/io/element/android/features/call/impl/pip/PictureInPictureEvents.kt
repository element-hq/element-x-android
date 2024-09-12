/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import io.element.android.features.call.impl.utils.PipController

sealed interface PictureInPictureEvents {
    data class SetPipController(val pipController: PipController) : PictureInPictureEvents
    data object EnterPictureInPicture : PictureInPictureEvents
    data class OnPictureInPictureModeChanged(val isInPip: Boolean) : PictureInPictureEvents
}
