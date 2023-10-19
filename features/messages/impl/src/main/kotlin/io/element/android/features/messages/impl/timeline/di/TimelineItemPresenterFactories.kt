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
import androidx.compose.runtime.staticCompositionLocalOf
import com.squareup.anvil.annotations.ContributesTo
import dagger.Module
import dagger.multibindings.Multibinds
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.RoomScope
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
 * Wrapper around the [TimelineItemPresenterFactory] map multi binding.
 *
 * Its only purpose is to provide a nicer type name than:
 * `@JvmSuppressWildcards Map<Class<out TimelineItemEventContent>, TimelineItemPresenterFactory<*, *>>`.
 *
 * A typealias would have been better but typealiases on Dagger types which use @JvmSuppressWildcards
 * currently make Dagger crash.
 *
 * Request this type from Dagger to access the [TimelineItemPresenterFactory] map multibinding.
 */
data class TimelineItemPresenterFactories @Inject constructor(
    val factories: @JvmSuppressWildcards Map<Class<out TimelineItemEventContent>, TimelineItemPresenterFactory<*, *>>,
)

/**
 * Provides a [TimelineItemPresenterFactories] to the composition.
 */
val LocalTimelineItemPresenterFactories = staticCompositionLocalOf {
    TimelineItemPresenterFactories(emptyMap())
}

/**
 * Creates and remembers a presenter for the given content.
 *
 * Will throw if the presenter is not found in the [TimelineItemPresenterFactory] map multi binding.
 */
@Composable
inline fun <reified C : TimelineItemEventContent, reified S : Any> TimelineItemPresenterFactories.rememberPresenter(
    content: C
): Presenter<S> = remember(content) {
    factories.getValue(C::class.java).let {
        @Suppress("UNCHECKED_CAST")
        (it as TimelineItemPresenterFactory<C, S>).create(content)
    }
}
