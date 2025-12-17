/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.di

import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import io.element.android.libraries.voiceplayer.api.aVoiceMessageState

/**
 * A fake [MediaItemPresenterFactories] for screenshot tests.
 */
fun aFakeMediaItemPresenterFactories() = MediaItemPresenterFactories(
    mapOf(
        Pair(
            MediaItem.Voice::class,
            MediaItemPresenterFactory<MediaItem.Voice, VoiceMessageState> { Presenter { aVoiceMessageState() } },
        ),
    )
)
