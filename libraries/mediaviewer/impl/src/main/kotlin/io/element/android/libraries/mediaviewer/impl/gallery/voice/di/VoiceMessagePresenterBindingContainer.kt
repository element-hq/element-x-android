/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.voice.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemEventContentKey
import io.element.android.libraries.mediaviewer.impl.gallery.di.MediaItemPresenterFactory
import io.element.android.libraries.mediaviewer.impl.gallery.voice.VoiceMessagePresenter
import io.element.android.libraries.mediaviewer.impl.model.MediaItem

@BindingContainer
@ContributesTo(RoomScope::class)
interface VoiceMessagePresenterBindingContainer {
    @Binds
    @IntoMap
    @MediaItemEventContentKey(MediaItem.Voice::class)
    fun bindVoiceMessagePresenterFactory(factory: VoiceMessagePresenter.Factory): MediaItemPresenterFactory<*, *>
}
