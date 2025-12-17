/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.gallery.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.mediaviewer.impl.model.MediaItem
import kotlin.reflect.KClass

/**
 * Container that declares the [MediaItemPresenterFactory] map multi binding.
 *
 * Its sole purpose is to support the case of an empty map multibinding.
 */
@BindingContainer
@ContributesTo(RoomScope::class)
interface MediaItemPresenterFactoriesModule {
    @Multibinds
    fun multiBindMediaItemPresenterFactories(): @JvmSuppressWildcards Map<KClass<out MediaItem.Event>, MediaItemPresenterFactory<*, *>>
}

/**
 * Room level caching layer for the [MediaItemPresenterFactory] instances.
 *
 * It will cache the presenter instances in the room scope, so that they can be
 * reused across recompositions of the gallery items that happen whenever an item
 * goes out of the [LazyColumn] viewport.
 */
@SingleIn(RoomScope::class)
@Inject
class MediaItemPresenterFactories(
    private val factories: @JvmSuppressWildcards Map<KClass<out MediaItem.Event>, MediaItemPresenterFactory<*, *>>,
) {
    private val presenters: MutableMap<MediaItem.Event, Presenter<*>> = mutableMapOf()

    /**
     * Creates and caches a presenter for the given content.
     *
     * Will throw if the presenter is not found in the [MediaItemPresenterFactory] map multi binding.
     *
     * @param C The [MediaItem.Event] subtype handled by this TimelineItem presenter.
     * @param S The state type produced by this timeline item presenter.
     * @param content The [MediaItem.Event] instance to create a presenter for.
     * @param contentClass The class of [content].
     * @return An instance of a TimelineItem presenter that will be cached in the room scope.
     */
    @Composable
    fun <C : MediaItem.Event, S : Any> rememberPresenter(
        content: C,
        contentClass: KClass<C>,
    ): Presenter<S> = remember(content) {
        presenters[content]?.let {
            @Suppress("UNCHECKED_CAST")
            it as Presenter<S>
        } ?: factories.getValue(contentClass).let {
            @Suppress("UNCHECKED_CAST")
            (it as MediaItemPresenterFactory<C, S>).create(content).apply {
                presenters[content] = this
            }
        }
    }
}

/**
 * Creates and caches a presenter for the given content.
 *
 * Will throw if the presenter is not found in the [MediaItemPresenterFactory] map multi binding.
 *
 * @param C The [MediaItem.Event] subtype handled by this TimelineItem presenter.
 * @param S The state type produced by this timeline item presenter.
 * @param content The [MediaItem.Event] instance to create a presenter for.
 * @return An instance of a TimelineItem presenter that will be cached in the room scope.
 */
@Composable
inline fun <reified C : MediaItem.Event, S : Any> MediaItemPresenterFactories.rememberPresenter(
    content: C
): Presenter<S> = rememberPresenter(
    content = content,
    contentClass = C::class
)
