/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.pip

import io.element.android.features.call.impl.utils.PipController

sealed interface PictureInPictureEvent {
    data class SetPipController(val pipController: PipController) : PictureInPictureEvent
    data object EnterPictureInPicture : PictureInPictureEvent
    data class OnPictureInPictureModeChanged(val isInPip: Boolean) : PictureInPictureEvent
}
