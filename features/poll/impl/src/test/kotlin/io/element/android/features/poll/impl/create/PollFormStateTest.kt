/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import com.google.common.truth.Truth.assertThat
import io.element.android.features.poll.impl.PollConstants
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.toImmutableList
import org.junit.Test

class PollFormStateTest {
    @Test
    fun `with new answer`() {
        val state = PollFormState.Empty
        val newState = state.withNewAnswer()
        assertThat(newState.answers).isEqualTo(listOf("", "", ""))
    }

    @Test
    fun `with new answer, given cannot add, doesn't add`() {
        val state = PollFormState.Empty.withBlankAnswers(PollConstants.MAX_ANSWERS)
        val newState = state.withNewAnswer()
        assertThat(newState).isEqualTo(state)
    }

    @Test
    fun `with answer deleted, given cannot delete, doesn't delete`() {
        val state = PollFormState.Empty
        val newState = state.withAnswerRemoved(0)
        assertThat(newState).isEqualTo(state)
    }

    @Test
    fun `with answer deleted, given can delete`() {
        val state = PollFormState.Empty.withNewAnswer()
        val newState = state.withAnswerRemoved(0)
        assertThat(newState).isEqualTo(PollFormState.Empty)
    }

    @Test
    fun `with answer changed`() {
        val state = PollFormState.Empty
        val newState = state.withAnswerChanged(1, "New answer")
        assertThat(newState).isEqualTo(PollFormState.Empty.copy(
            answers = listOf("", "New answer").toImmutableList()
        ))
    }

    @Test
    fun `with answer changed, given it is too long, truncates`() {
        val tooLongAnswer = "a".repeat(PollConstants.MAX_ANSWER_LENGTH * 2)
        val truncatedAnswer = "a".repeat(PollConstants.MAX_ANSWER_LENGTH)
        val state = PollFormState.Empty
        val newState = state.withAnswerChanged(1, tooLongAnswer)
        assertThat(newState).isEqualTo(PollFormState.Empty.copy(
            answers = listOf("", truncatedAnswer).toImmutableList()
        ))
    }

    @Test
    fun `can add answer is true when it does not have max answers`() {
        val state = PollFormState.Empty.withBlankAnswers(PollConstants.MAX_ANSWERS - 1)
        assertThat(state.canAddAnswer).isTrue()
    }

    @Test
    fun `can add answer is false when it has max answers`() {
        val state = PollFormState.Empty.withBlankAnswers(PollConstants.MAX_ANSWERS)
        assertThat(state.canAddAnswer).isFalse()
    }

    @Test
    fun `can delete answer is false when it has min answers`() {
        val state = PollFormState.Empty.withBlankAnswers(PollConstants.MIN_ANSWERS)
        assertThat(state.canDeleteAnswer).isFalse()
    }

    @Test
    fun `can delete answer is true when it has more than min answers`() {
        val numAnswers = PollConstants.MIN_ANSWERS + 1
        val state = PollFormState.Empty.withBlankAnswers(numAnswers)
        assertThat(state.canDeleteAnswer).isTrue()
    }

    @Test
    fun `is valid is true when it is valid`() {
        val state = aValidPollFormState()
        assertThat(state.isValid).isTrue()
    }

    @Test
    fun `is valid is false when question is blank`() {
        val state = aValidPollFormState().copy(question = "")
        assertThat(state.isValid).isFalse()
    }

    @Test
    fun `is valid is false when not enough answers`() {
        val state = aValidPollFormState().copy(answers = listOf("").toImmutableList())
        assertThat(state.isValid).isFalse()
    }

    @Test
    fun `is valid is false when one answer is blank`() {
        val state = aValidPollFormState().withNewAnswer()
        assertThat(state.isValid).isFalse()
    }

    @Test
    fun `poll kind when is disclosed`() {
        val state = PollFormState.Empty.copy(isDisclosed = true)
        assertThat(state.pollKind).isEqualTo(PollKind.Disclosed)
    }

    @Test
    fun `poll kind when is not disclosed`() {
        val state = PollFormState.Empty.copy(isDisclosed = false)
        assertThat(state.pollKind).isEqualTo(PollKind.Undisclosed)
    }
}

private fun aValidPollFormState(): PollFormState {
    return PollFormState.Empty.copy(
        question = "question",
        answers = listOf("answer1", "answer2").toImmutableList(),
        isDisclosed = true,
    )
}

private fun PollFormState.withBlankAnswers(numAnswers: Int): PollFormState =
    copy(answers = List(numAnswers) { "" }.toImmutableList())
