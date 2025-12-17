/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.di

import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.libraries.architecture.Presenter

/**
 * A factory for a [Presenter] associated with a timeline item.
 *
 * Implementations should be annotated with [dev.zacsweers.metro.AssistedFactory] to be created by the dependency injection library.
 *
 * @param C The timeline item's [TimelineItemEventContent] subtype.
 * @param S The [Presenter]'s state class.
 * @return A [Presenter] that produces a state of type [S] for the given content of type [C].
 */
fun interface TimelineItemPresenterFactory<C : TimelineItemEventContent, S : Any> {
    fun create(content: C): Presenter<S>
}
