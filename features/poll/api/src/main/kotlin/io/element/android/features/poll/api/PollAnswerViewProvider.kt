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

fun aPollAnswerItemList() = listOf(
    aPollAnswerItem(
        answer = PollAnswer("option_1", "Italian \uD83C\uDDEE\uD83C\uDDF9"),
        votesCount = 2,
        progress = 0.2f
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_2", "Chinese \uD83C\uDDE8\uD83C\uDDF3"),
        votesCount = 5,
        progress = 0.5f
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_3", "Brazilian \uD83C\uDDE7\uD83C\uDDF7"),
        votesCount = 3,
        progress = 0.3f
    ),
    aPollAnswerItem(),
)

fun aPollAnswerItem(
    answer: PollAnswer = PollAnswer(
        "option_4",
        "French \uD83C\uDDEB\uD83C\uDDF7 But make it a very very very long option then this should just keep expanding"
    ),
    votesCount: Int = 2,
    progress: Float = 0.2f,
) = PollAnswerItem(answer, votesCount, progress)
