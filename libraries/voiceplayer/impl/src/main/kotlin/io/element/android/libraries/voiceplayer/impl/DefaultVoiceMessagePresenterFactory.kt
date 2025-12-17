/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.voiceplayer.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.voiceplayer.api.VoiceMessagePresenterFactory
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.services.analytics.api.AnalyticsService
import kotlinx.coroutines.CoroutineScope
import kotlin.time.Duration

@ContributesBinding(RoomScope::class)
class DefaultVoiceMessagePresenterFactory(
    private val analyticsService: AnalyticsService,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
    private val voiceMessagePlayerFactory: VoiceMessagePlayer.Factory,
) : VoiceMessagePresenterFactory {
    override fun createVoiceMessagePresenter(
        eventId: EventId?,
        mediaSource: MediaSource,
        mimeType: String?,
        filename: String?,
        duration: Duration,
    ): Presenter<VoiceMessageState> {
        val player = voiceMessagePlayerFactory.create(
            eventId = eventId,
            mediaSource = mediaSource,
            mimeType = mimeType,
            filename = filename,
        )

        return VoiceMessagePresenter(
            analyticsService = analyticsService,
            sessionCoroutineScope = sessionCoroutineScope,
            player = player,
            eventId = eventId,
            duration = duration,
        )
    }
}
