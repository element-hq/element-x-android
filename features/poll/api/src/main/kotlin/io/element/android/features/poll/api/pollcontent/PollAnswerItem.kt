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

package io.element.android.features.poll.api.pollcontent

import io.element.android.libraries.matrix.api.poll.PollAnswer

/**
 * UI model for a [PollAnswer].
 *
 * @property answer the poll answer.
 * @property isSelected whether the user has selected this answer.
 * @property isEnabled whether the answer can be voted.
 * @property isWinner whether this is the winner answer in the poll.
 * @property isDisclosed whether the votes for this answer should be disclosed.
 * @property votesCount the number of votes for this answer.
 * @property percentage the percentage of votes for this answer.
 */
data class PollAnswerItem(
    val answer: PollAnswer,
    val isSelected: Boolean,
    val isEnabled: Boolean,
    val isWinner: Boolean,
    val isDisclosed: Boolean,
    val votesCount: Int,
    val percentage: Float,
)
