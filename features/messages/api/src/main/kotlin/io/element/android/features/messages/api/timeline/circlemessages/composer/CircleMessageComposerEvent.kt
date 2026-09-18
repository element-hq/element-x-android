/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline.circlemessages.composer

import androidx.camera.view.PreviewView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

sealed interface CircleMessageComposerEvent {
    data object Start : CircleMessageComposerEvent
    data object Stop : CircleMessageComposerEvent
    data object Cancel : CircleMessageComposerEvent
    data object FlipCamera : CircleMessageComposerEvent
    data class SetLinearZoom(val linearZoom: Float) : CircleMessageComposerEvent
    data object AcceptCameraPermissionRationale : CircleMessageComposerEvent
    data object DismissCameraPermissionRationale : CircleMessageComposerEvent
    data object AcceptAudioPermissionRationale : CircleMessageComposerEvent
    data object DismissAudioPermissionRationale : CircleMessageComposerEvent
    data class LifecycleEvent(val event: Lifecycle.Event) : CircleMessageComposerEvent
    data object DismissSendFailureDialog : CircleMessageComposerEvent
    data class BindPreview(
        val previewView: PreviewView,
        val lifecycleOwner: LifecycleOwner,
    ) : CircleMessageComposerEvent
}
