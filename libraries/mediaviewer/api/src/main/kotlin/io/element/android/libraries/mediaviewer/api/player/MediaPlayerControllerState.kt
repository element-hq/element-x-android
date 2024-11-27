/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.player

import io.element.android.libraries.mediaviewer.api.local.PlayableState

data class MediaPlayerControllerState(
    val isVisible: Boolean,
    val playableState: PlayableState.Playable,
)
