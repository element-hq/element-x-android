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

package io.element.android.features.messages.impl.timeline.model

import io.element.android.libraries.core.extensions.ellipsize

/**
 * Length at which we ellipsize a reaction key for display
 *
 * Reactions can be free text, so we need to limit the length
 * displayed on screen.
 */
private const val MAX_DISPLAY_CHARS = 16

/**
 * @property key the full reaction key (e.g. "👍", "YES!")
 * @property count the number of users who reacted with this key
 * @property isHighlighted true if the reaction has (also) been sent by the current user.
 */
data class AggregatedReaction(
    val key: String,
    val count: Int,
    val isHighlighted: Boolean = false
) {

    /**
     * The key to be displayed on screen.
     *
     * See [MAX_DISPLAY_CHARS].
     */
    val displayKey: String by lazy {
        key.ellipsize(MAX_DISPLAY_CHARS)
    }
}
