/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.composer

import androidx.compose.runtime.Stable
import io.element.android.libraries.textcomposer.model.VoiceMessageState

@Stable
data class VoiceMessageComposerState(
    val voiceMessageState: VoiceMessageState,
    val showPermissionRationaleDialog: Boolean,
    val showSendFailureDialog: Boolean,
    val keepScreenOn: Boolean,
    val eventSink: (VoiceMessageComposerEvents) -> Unit,
)
