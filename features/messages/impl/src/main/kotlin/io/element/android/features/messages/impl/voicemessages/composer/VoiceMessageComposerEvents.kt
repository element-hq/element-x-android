/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.lifecycle.Lifecycle
import io.element.android.libraries.textcomposer.model.VoiceMessagePlayerEvent
import io.element.android.libraries.textcomposer.model.VoiceMessageRecorderEvent

sealed interface VoiceMessageComposerEvents {
    data class RecorderEvent(
        val recorderEvent: VoiceMessageRecorderEvent
    ) : VoiceMessageComposerEvents
    data class PlayerEvent(
        val playerEvent: VoiceMessagePlayerEvent,
    ) : VoiceMessageComposerEvents
    data object SendVoiceMessage : VoiceMessageComposerEvents
    data object DeleteVoiceMessage : VoiceMessageComposerEvents
    data object AcceptPermissionRationale : VoiceMessageComposerEvents
    data object DismissPermissionsRationale : VoiceMessageComposerEvents
    data class LifecycleEvent(val event: Lifecycle.Event) : VoiceMessageComposerEvents
    data object DismissSendFailureDialog : VoiceMessageComposerEvents
}
