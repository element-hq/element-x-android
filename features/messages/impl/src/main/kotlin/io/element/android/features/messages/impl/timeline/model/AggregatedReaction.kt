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
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Length at which we ellipsize a reaction key for display
 *
 * Reactions can be free text, so we need to limit the length
 * displayed on screen.
 */
internal const val MAX_REACTION_LENGTH_CHARS = 16

/**
 * @property currentUserId the ID of the currently logged in user
 * @property key the full reaction key (e.g. "👍", "YES!")
 * @property senders the list of users who sent the reactions
 */
data class AggregatedReaction(
    val currentUserId: UserId,
    val key: String,
    val senders: List<AggregatedReactionSender>
) {
    /**
     * The key to be displayed on screen.
     *
     * See [MAX_REACTION_LENGTH_CHARS].
     */
    val displayKey: String by lazy {
        key.ellipsize(MAX_REACTION_LENGTH_CHARS)
    }

    /**
     * The number of users who reacted with this key.
     */
    val count: Int by lazy {
        senders.count()
    }

    /**
     * True if the reaction has (also) been sent by the current user.
     */
    val isHighlighted: Boolean by lazy {
        senders.any { it.senderId.value == currentUserId.value }
    }
}
