/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.voice

import androidx.compose.runtime.Composable
import com.squareup.anvil.annotations.ContributesTo
import dagger.Binds
import dagger.Module
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.multibindings.IntoMap
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemEventContentKey
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemPresenterFactory
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import io.element.android.libraries.voiceplayer.api.VoiceMessagePresenterFactory
import io.element.android.libraries.voiceplayer.api.VoiceMessageState
import kotlin.time.Duration

@Module
@ContributesTo(RoomScope::class)
interface VoiceMessagePresenterModule {
    @Binds
    @IntoMap
    @MediaItemEventContentKey(MediaItem.Voice::class)
    fun bindVoiceMessagePresenterFactory(factory: VoiceMessagePresenter.Factory): MediaItemPresenterFactory<*, *>
}

class VoiceMessagePresenter @AssistedInject constructor(
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
