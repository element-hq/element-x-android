/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.test.timeline.voicemessages.composer

import io.element.android.features.messages.impl.voicemessages.composer.DefaultVoiceMessageComposerPresenter
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerPlayer
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.test.room.FakeJoinedRoom
import io.element.android.libraries.mediaplayer.test.FakeMediaPlayer
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.impl.DefaultMediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaOptimizationConfigProvider
import io.element.android.libraries.mediaupload.test.FakeMediaPreProcessor
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import io.element.android.libraries.voicerecorder.test.FakeVoiceRecorder
import io.element.android.services.analytics.test.FakeAnalyticsService
import kotlinx.coroutines.CoroutineScope

class FakeDefaultVoiceMessageComposerPresenterFactory(
    private val sessionCoroutineScope: CoroutineScope,
    private val mediaSender: MediaSender = DefaultMediaSender(
        preProcessor = FakeMediaPreProcessor(),
        room = FakeJoinedRoom(),
        timelineMode = Timeline.Mode.Live,
        mediaOptimizationConfigProvider = FakeMediaOptimizationConfigProvider(),
    ),
) : DefaultVoiceMessageComposerPresenter.Factory {
    override fun create(timelineMode: Timeline.Mode): DefaultVoiceMessageComposerPresenter {
        return DefaultVoiceMessageComposerPresenter(
            sessionCoroutineScope = sessionCoroutineScope,
            timelineMode = timelineMode,
            voiceRecorder = FakeVoiceRecorder(),
            analyticsService = FakeAnalyticsService(),
            mediaSenderFactory = { mediaSender },
            player = VoiceMessageComposerPlayer(
                mediaPlayer = FakeMediaPlayer(),
                sessionCoroutineScope = sessionCoroutineScope,
            ),
            messageComposerContext = FakeMessageComposerContext(),
            permissionsPresenterFactory = FakePermissionsPresenterFactory(),
        )
    }
}
