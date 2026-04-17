/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

sealed interface VoiceMessageRecorderEvent {
    data object Start : VoiceMessageRecorderEvent
    data object Cancel : VoiceMessageRecorderEvent

    /** Swipe-up or tap triggered — locks recording so user can release the button. */
    data object Lock : VoiceMessageRecorderEvent

    /** Release in hold mode — stops recording and sends immediately. */
    data object StopAndSend : VoiceMessageRecorderEvent
    data object Pause : VoiceMessageRecorderEvent
    data object Resume : VoiceMessageRecorderEvent

    /** Stop recording and enter preview mode for playback review. */
    data object StopAndPreview : VoiceMessageRecorderEvent
}
