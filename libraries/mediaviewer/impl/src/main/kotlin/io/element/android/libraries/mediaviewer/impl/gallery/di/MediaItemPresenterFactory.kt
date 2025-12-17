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

/**
 * A factory for a [Presenter] associated with a timeline item.
 *
 * Implementations should be annotated with [dev.zacsweers.metro.AssistedFactory] to be created.
 *
 * @param C The timeline item's [MediaItem.Event] subtype.
 * @param S The [Presenter]'s state class.
 * @return A [Presenter] that produces a state of type [S] for the given content of type [C].
 */
fun interface MediaItemPresenterFactory<C : MediaItem.Event, S : Any> {
    fun create(content: C): Presenter<S>
}
