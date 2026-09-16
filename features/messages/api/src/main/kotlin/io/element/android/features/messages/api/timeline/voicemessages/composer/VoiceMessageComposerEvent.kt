/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline.voicemessages.composer

import androidx.lifecycle.Lifecycle
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent

sealed interface VoiceMessageComposerEvent {
    data class RecorderEvent(
        val recorderEvent: VoiceMessageRecorderEvent
    ) : VoiceMessageComposerEvent
    data class PlayerEvent(
        val playerEvent: VoiceMessagePlayerEvent,
    ) : VoiceMessageComposerEvent
    data object SendVoiceMessage : VoiceMessageComposerEvent
    data object DeleteVoiceMessage : VoiceMessageComposerEvent
    data object AcceptPermissionRationale : VoiceMessageComposerEvent
    data object DismissPermissionsRationale : VoiceMessageComposerEvent
    data class LifecycleEvent(val event: Lifecycle.Event) : VoiceMessageComposerEvent
    data object DismissSendFailureDialog : VoiceMessageComposerEvent
}
