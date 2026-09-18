/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline.circlemessages.composer

import androidx.compose.runtime.Stable

@Stable
data class CircleMessageComposerState(
    val isRecording: Boolean,
    val useFrontCamera: Boolean,
    val isSwitchingCamera: Boolean,
    val showCameraPermissionRationaleDialog: Boolean,
    val showAudioPermissionRationaleDialog: Boolean,
    val showSendFailureDialog: Boolean,
    val keepScreenOn: Boolean,
    val eventSink: (CircleMessageComposerEvent) -> Unit,
)
