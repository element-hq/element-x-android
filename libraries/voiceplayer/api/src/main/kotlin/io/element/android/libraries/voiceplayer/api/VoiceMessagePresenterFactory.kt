/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.api

import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import kotlin.time.Duration

/**
 * Creates the presenter of a single voice message player, one per voice message shown in a timeline.
 */
interface VoiceMessagePresenterFactory {
    /**
     * @param eventId the event carrying the voice message, or `null` while it is still a local echo.
     * @param mediaSource where the audio is fetched from.
     * @param mimeType the MIME type of the audio, or `null` when the event did not state it.
     * @param filename the file name to display, or `null` when the event did not state it.
     * @param duration the length of the recording, used to render the waveform before the audio is downloaded.
     */
    fun createVoiceMessagePresenter(
        eventId: EventId?,
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
        duration: Duration,
    ): Presenter<VoiceMessageState>
}
