/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.di

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.aVoiceMessageState

/**
 * A fake [TimelineItemPresenterFactories] for screenshot tests.
 */
fun aFakeTimelineItemPresenterFactories() = TimelineItemPresenterFactories(
    mapOf(
        Pair(
            TimelineItemVoiceContent::class,
            TimelineItemPresenterFactory<TimelineItemVoiceContent, VoiceMessageState> { Presenter { aVoiceMessageState() } },
        ),
    )
)
