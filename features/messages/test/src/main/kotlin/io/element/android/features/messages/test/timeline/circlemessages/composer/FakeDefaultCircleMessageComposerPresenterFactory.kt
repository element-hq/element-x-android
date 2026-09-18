/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.test.timeline.circlemessages.composer

import io.element.android.features.messages.impl.circlemessages.composer.DefaultCircleMessageComposerPresenter
import io.element.android.features.messages.test.FakeMessageComposerContext
import io.element.android.libraries.circlerecorder.test.FakeCircleRecorder
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaplayer.test.FakeAudioFocus
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaupload.test.FakeMediaSender
import io.element.android.libraries.permissions.test.FakePermissionsPresenterFactory
import kotlinx.coroutines.CoroutineScope

class FakeDefaultCircleMessageComposerPresenterFactory(
    private val sessionCoroutineScope: CoroutineScope,
    private val mediaSender: MediaSender = FakeMediaSender(),
) : DefaultCircleMessageComposerPresenter.Factory {
    override fun create(timelineMode: Timeline.Mode): DefaultCircleMessageComposerPresenter {
        return DefaultCircleMessageComposerPresenter(
            sessionCoroutineScope = sessionCoroutineScope,
            timelineMode = timelineMode,
            circleRecorder = FakeCircleRecorder(),
            audioFocus = FakeAudioFocus(
                requestAudioFocusResult = { _, _ -> },
                releaseAudioFocusResult = { },
            ),
            mediaSenderFactory = { mediaSender },
            messageComposerContext = FakeMessageComposerContext(),
            permissionsPresenterFactory = FakePermissionsPresenterFactory(),
        )
    }
}
