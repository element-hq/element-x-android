/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
