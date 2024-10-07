/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.di

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.voicemessages.timeline.VoiceMessageState
import io.element.android.features.messages.impl.voicemessages.timeline.aVoiceMessageState
import io.element.android.libraries.architecture.Presenter

/**
 * A fake [TimelineItemPresenterFactories] for screenshot tests.
 */
fun aFakeTimelineItemPresenterFactories() = TimelineItemPresenterFactories(
    mapOf(
        Pair(
            TimelineItemVoiceContent::class.java,
            TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> { Presenter { aVoiceMessageState() } },
        ),
    )
)
