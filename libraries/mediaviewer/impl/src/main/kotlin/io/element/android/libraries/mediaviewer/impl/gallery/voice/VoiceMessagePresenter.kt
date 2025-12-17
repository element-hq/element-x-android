/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.voice

import androidx.compose.runtime.Composable
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemEventContentKey
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemPresenterFactory
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.voiceplayer.api.VoiceMessagePresenterFactory
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import kotlin.time.Duration

@BindingContainer
@ContributesTo(RoomScope::class)
interface VoiceMessagePresenterModule {
    @Binds
    @IntoMap
    @MediaItemEventContentKey(MediaItem.Voice::class)
    fun bindVoiceMessagePresenterFactory(factory: VoiceMessagePresenter.Factory): MediaItemPresenterFactory<*, *>
}

@AssistedInject
class VoiceMessagePresenter(
    voiceMessagePresenterFactory: VoiceMessagePresenterFactory,
    @Assisted private val item: MediaItem.Voice,
) : Presenter<VoiceMessageState> {
    @AssistedFactory
    fun interface Factory : MediaItemPresenterFactory<MediaItem.Voice, VoiceMessageState> {
        override fun create(content: MediaItem.Voice): VoiceMessagePresenter
    }

    private val presenter = voiceMessagePresenterFactory.createVoiceMessagePresenter(
        eventId = item.eventId,
        mediaSource = item.mediaSource,
        mimeType = item.mediaInfo.mimeType,
        filename = item.mediaInfo.filename,
        // TODO Get the duration for the fallback?
        duration = Duration.ZERO,
    )

    @Composable
    override fun present(): VoiceMessageState {
        return presenter.present()
    }
}
