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

package io.element.android.features.poll.api

import io.element.android.libraries.matrix.api.poll.PollAnswer
import kotlinx.collections.immutable.persistentListOf

fun aPollAnswerItemList(isDisclosed: Boolean = true) = persistentListOf(
    aPollAnswerItem(
        answer = PollAnswer("option_1", "Italian \uD83C\uDDEE\uD83C\uDDF9"),
        isDisclosed = isDisclosed,
        votesCount = 5,
        percentage = 0.5f
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_2", "Chinese \uD83C\uDDE8\uD83C\uDDF3"),
        isDisclosed = isDisclosed,
        votesCount = 0,
        percentage = 0f
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_3", "Brazilian \uD83C\uDDE7\uD83C\uDDF7"),
        isDisclosed = isDisclosed,
        isSelected = true,
        votesCount = 1,
        percentage = 0.1f
    ),
    aPollAnswerItem(isDisclosed = isDisclosed),
)

fun aPollAnswerItem(
    answer: PollAnswer = PollAnswer(
        "option_4",
        "French \uD83C\uDDEB\uD83C\uDDF7 But make it a very very very long option then this should just keep expanding"
    ),
    isSelected: Boolean = false,
    isDisclosed: Boolean = true,
    votesCount: Int = 4,
    percentage: Float = 0.4f,
) = PollAnswerItem(
    answer = answer,
    isSelected = isSelected,
    isDisclosed = isDisclosed,
    votesCount = votesCount,
    percentage = percentage
)
