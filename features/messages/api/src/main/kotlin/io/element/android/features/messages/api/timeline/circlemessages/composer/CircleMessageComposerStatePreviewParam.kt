/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline.circlemessages.composer

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class CircleMessageComposerStatePreviewParam : PreviewParameterProvider<CircleMessageComposerState> {
    override val values: Sequence<CircleMessageComposerState>
        get() = sequenceOf(
            aCircleMessageComposerState(),
            aCircleMessageComposerState(isRecording = true),
            aCircleMessageComposerState(showCameraPermissionRationaleDialog = true),
        )
}

fun aCircleMessageComposerState(
    isRecording: Boolean = false,
    useFrontCamera: Boolean = true,
    isSwitchingCamera: Boolean = false,
    keepScreenOn: Boolean = false,
    showCameraPermissionRationaleDialog: Boolean = false,
    showAudioPermissionRationaleDialog: Boolean = false,
    showSendFailureDialog: Boolean = false,
) = CircleMessageComposerState(
    isRecording = isRecording,
    useFrontCamera = useFrontCamera,
    isSwitchingCamera = isSwitchingCamera,
    showCameraPermissionRationaleDialog = showCameraPermissionRationaleDialog,
    showAudioPermissionRationaleDialog = showAudioPermissionRationaleDialog,
    showSendFailureDialog = showSendFailureDialog,
    keepScreenOn = keepScreenOn,
    eventSink = {},
)
