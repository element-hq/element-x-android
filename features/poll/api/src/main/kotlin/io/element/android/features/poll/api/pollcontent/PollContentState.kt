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

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.ImmutableList

/**
 * UI model for a PollContent.
 * @property eventId the event id of the poll.
 * @property question the poll question.
 * @property answerItems the list of answers.
 * @property pollKind the kind of poll.
 * @property isPollEditable whether the poll is editable.
 * @property isPollEnded whether the poll is ended.
 * @property isMine whether the poll has been created by me.
 */
data class PollContentState(
    val eventId: EventId?,
    val question: String,
    val answerItems: ImmutableList<PollAnswerItem>,
    val pollKind: PollKind,
    val isPollEditable: Boolean,
    val isPollEnded: Boolean,
    val isMine: Boolean,
)
