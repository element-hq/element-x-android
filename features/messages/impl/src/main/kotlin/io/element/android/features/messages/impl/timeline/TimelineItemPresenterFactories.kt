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

package io.element.android.features.messages.impl.timeline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import dagger.MapKey
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContent
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject
import kotlin.reflect.KClass

/**
 * Annotation to add factories of type TimelineItemPresenterFactory<*, *> to the Dagger map multi binding.
 */
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class TimelineItemEventContentKey(val value: KClass<out TimelineItemEventContent>)

/**
 * Super type for an assisted injected presenter factory for timeline items.
 */
fun interface TimelineItemPresenterFactory<C : TimelineItemEventContent, S : Any> {
    fun create(content: C): Presenter<S>
}

/**
 * Wrapper around the Dagger map multi binding to provide a nicer type name.
 *
 * A typealias would have been better but Dagger type aliases with @JvmSuppressWildcards currently crash Dagger.
 */
data class TimelineItemPresenterFactories @Inject constructor(
    val factories: @JvmSuppressWildcards Map<Class<out TimelineItemEventContent>, TimelineItemPresenterFactory<*, *>>,
)

/**
 * Create and remember a presenter for the given content.
 *
 * Will crash if the presenter is not found in the @TimelineItemEventContentKey map multi binding.
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

val LocalTimelineItemPresenterFactories = staticCompositionLocalOf {
    TimelineItemPresenterFactories(emptyMap())
}
