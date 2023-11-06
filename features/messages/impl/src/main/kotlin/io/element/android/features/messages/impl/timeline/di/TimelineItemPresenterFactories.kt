/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.multibindings.Multibinds
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

/**
 * Dagger module that declares the [TimelineItemPresenterFactory] map multi binding.
 *
 * Its sole purpose is to support the case of an empty map multibinding.
 */
@Module
@ContributesTo(RoomScope::class)
interface TimelineItemPresenterFactoriesModule {
    @Multibinds
    fun multiBindTimelineItemPresenterFactories(): @JvmSuppressWildcards Map<Class<out TimelineItemEventContent>, TimelineItemPresenterFactory<*, *>>
}

/**
 * Room level caching layer for the [TimelineItemPresenterFactory] instances.
 *
 * It will cache the presenter instances in the room scope, so that they can be
 * reused across recompositions of the timeline items that happen whenever an item
 * goes out of the [LazyColumn] viewport.
 */
@SingleIn(RoomScope::class)
class TimelineItemPresenterFactories @Inject constructor(
    private val factories: @JvmSuppressWildcards Map<Class<out TimelineItemEventContent>, TimelineItemPresenterFactory<*, *>>,
) {
    private val presenters: MutableMap<TimelineItemEventContent, Presenter<*>> = mutableMapOf()

    /**
     * Creates and caches a presenter for the given content.
     *
     * Will throw if the presenter is not found in the [TimelineItemPresenterFactory] map multi binding.
     *
     * @param C The [TimelineItemEventContent] subtype handled by this TimelineItem presenter.
     * @param S The state type produced by this timeline item presenter.
     * @param content The [TimelineItemEventContent] instance to create a presenter for.
     * @param contentClass The class of [content].
     * @return An instance of a TimelineItem presenter that will be cached in the room scope.
     */
    @Composable
    fun <C : TimelineItemEventContent, S : Any> rememberPresenter(
        content: C,
        contentClass: Class<C>,
    ): Presenter<S> = remember(content) {
        presenters[content]?.let {
            @Suppress("UNCHECKED_CAST")
            it as Presenter<S>
        } ?: factories.getValue(contentClass).let {
            @Suppress("UNCHECKED_CAST")
            (it as TimelineItemPresenterFactory<C, S>).create(content).apply {
                presenters[content] = this
            }
        }
    }
}

/**
 * Creates and caches a presenter for the given content.
 *
 * Will throw if the presenter is not found in the [TimelineItemPresenterFactory] map multi binding.
 *
 * @param C The [TimelineItemEventContent] subtype handled by this TimelineItem presenter.
 * @param S The state type produced by this timeline item presenter.
 * @param content The [TimelineItemEventContent] instance to create a presenter for.
 * @return An instance of a TimelineItem presenter that will be cached in the room scope.
 */
@Composable
inline fun <reified C : TimelineItemEventContent, S : Any> TimelineItemPresenterFactories.rememberPresenter(
    content: C
): Presenter<S> = rememberPresenter(
    content = content,
    contentClass = C::class.java
)
