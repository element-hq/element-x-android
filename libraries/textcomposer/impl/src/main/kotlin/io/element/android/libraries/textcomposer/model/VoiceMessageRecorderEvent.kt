/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

sealed interface VoiceMessageRecorderEvent {
    data object Start : VoiceMessageRecorderEvent
    data object Stop : VoiceMessageRecorderEvent
    data object Cancel : VoiceMessageRecorderEvent
}
