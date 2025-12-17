/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import io.element.android.features.poll.impl.PollConstants
import io.element.android.features.poll.impl.PollConstants.MIN_ANSWERS
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * Represents the state of the poll creation / edit form.
 *
 * Save this state using [pollFormStateSaver].
 */
data class PollFormState(
    val question: String,
    val answers: ImmutableList<String>,
    val isDisclosed: Boolean,
) {
    companion object {
        val Empty = PollFormState(
            question = "",
            answers = MutableList(MIN_ANSWERS) { "" }.toImmutableList(),
            isDisclosed = true,
        )
    }

    val pollKind
        get() = when (isDisclosed) {
            true -> PollKind.Disclosed
            false -> PollKind.Undisclosed
        }

    /**
     * Create a copy of the [PollFormState] with a new blank answer added.
     *
     * If the maximum number of answers has already been reached an answer is not added.
     */
    fun withNewAnswer(): PollFormState {
        if (!canAddAnswer) {
            return this
        }

        return copy(answers = (answers + "").toImmutableList())
    }

    /**
     * Create a copy of the [PollFormState] with the answer at [index] removed.
     *
     * If the answer doesn't exist or can't be removed, the state is unchanged.
     *
     * @param index the index of the answer to remove.
     *
     * @return a new [PollFormState] with the answer at [index] removed.
     */
    fun withAnswerRemoved(index: Int): PollFormState {
        if (!canDeleteAnswer) {
            return this
        }

        return copy(answers = answers.filterIndexed { i, _ -> i != index }.toImmutableList())
    }

    /**
     * Create a copy of the [PollFormState] with the answer at [index] changed.
     *
     * If the new answer is longer than [PollConstants.MAX_ANSWER_LENGTH], it will be truncated.
     *
     * @param index the index of the answer to change.
     * @param rawAnswer the new answer as the user typed it.
     *
     * @return a new [PollFormState] with the answer at [index] changed.
     */
    fun withAnswerChanged(index: Int, rawAnswer: String): PollFormState =
        copy(answers = answers.toMutableList().apply {
            this[index] = rawAnswer.take(PollConstants.MAX_ANSWER_LENGTH)
        }.toImmutableList())

    /**
     * Whether a new answer can be added.
     */
    val canAddAnswer get() = answers.size < PollConstants.MAX_ANSWERS

    /**
     * Whether any answer can be deleted.
     */
    val canDeleteAnswer get() = answers.size > MIN_ANSWERS

    /**
     * Whether the form is currently valid.
     */
    val isValid get() = question.isNotBlank() && answers.size >= MIN_ANSWERS && answers.all { it.isNotBlank() }
}

/**
 * A [Saver] for [PollFormState].
 */
internal val pollFormStateSaver = mapSaver(
    save = {
        mutableMapOf(
            "question" to it.question,
            "answers" to it.answers.toTypedArray(),
            "isDisclosed" to it.isDisclosed,
        )
    },
    restore = { saved ->
        PollFormState(
            question = saved["question"] as String,
            answers = (saved["answers"] as Array<*>).map { it as String }.toImmutableList(),
            isDisclosed = saved["isDisclosed"] as Boolean,
        )
    }
)
