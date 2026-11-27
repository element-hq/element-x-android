/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.pollcontent

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import kotlinx.collections.immutable.persistentListOf

/**
 * The states are ordered following the lifecycle of a poll: created, open, voted, and ended.
 */
class PollContentStatePreviewParam : PreviewParameterProvider<PollContentState> {
    override val values: Sequence<PollContentState>
        get() = sequenceOf(
            // Created
            aPollContentState(
                question = "My poll, editable, no votes yet",
                isMine = true,
                hasVotes = false,
                hasMyVote = false,
            ),
            // Open, not voted
            aPollContentState(
                question = "Disclosed poll, single choice, not voted",
                hasMyVote = false,
            ),
            aPollContentState(
                question = "Disclosed poll, multiple choice (max 3), not voted",
                maxSelections = 3u,
                hasMyVote = false,
            ),
            // Open, voted
            aPollContentState(
                question = "Disclosed poll, single choice, voted",
            ),
            aPollContentState(
                question = "Undisclosed poll, single choice, voted",
                pollKind = PollKind.Undisclosed,
                showVotes = false,
            ),
            aPollContentState(
                question = "Disclosed poll, multiple choice (max 3), 1 answer selected",
                maxSelections = 3u,
            ),
            aPollContentState(
                question = "Disclosed poll, multiple choice (max 2), limit reached",
                maxSelections = 2u,
                answerItems = aMultipleChoiceAnswerItemList(isEnded = false),
            ),
            aPollContentState(
                question = "My poll, not editable, voted",
                isMine = true,
                isPollEditable = false,
            ),
            // Ended
            aPollContentState(
                question = "Ended poll, single choice, voted",
                isEnded = true,
            ),
            aPollContentState(
                question = "Ended poll, multiple choice (max 2), voted",
                maxSelections = 2u,
                isEnded = true,
                answerItems = aMultipleChoiceAnswerItemList(isEnded = true),
            ),
            aPollContentState(
                question = "Ended poll, single choice, tie between 2 answers",
                isEnded = true,
                answerItems = aTieAnswerItemList(),
            ),
            aPollContentState(
                question = "Ended poll, no votes",
                isEnded = true,
                hasVotes = false,
                hasMyVote = false,
            ),
            aPollContentState(
                question = "My ended poll, single choice, voted",
                isMine = true,
                isEnded = true,
            ),
        )
}

/**
 * 3 answers, with the first and the third ones selected.
 */
private fun aMultipleChoiceAnswerItemList(
    isEnded: Boolean,
) = persistentListOf(
    aPollAnswerItem(
        answer = PollAnswer("option_1", "Italian 🇮🇹"),
        isSelected = true,
        isEnabled = !isEnded,
        isWinner = isEnded,
        votesCount = 3,
        percentage = 0.5f,
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_2", "Chinese 🇨🇳"),
        isEnabled = false,
        votesCount = 1,
        percentage = 0.17f,
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_3", "Brazilian 🇧🇷"),
        isSelected = true,
        isEnabled = !isEnded,
        votesCount = 2,
        percentage = 0.33f,
    ),
)

/**
 * 3 answers of an ended poll, with the first two ones winning with the same number of votes, the second one selected.
 */
private fun aTieAnswerItemList() = persistentListOf(
    aPollAnswerItem(
        answer = PollAnswer("option_1", "Italian 🇮🇹"),
        isEnabled = false,
        isWinner = true,
        votesCount = 2,
        percentage = 0.4f,
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_2", "Chinese 🇨🇳"),
        isSelected = true,
        isEnabled = false,
        isWinner = true,
        votesCount = 2,
        percentage = 0.4f,
    ),
    aPollAnswerItem(
        answer = PollAnswer("option_3", "Brazilian 🇧🇷"),
        isEnabled = false,
        votesCount = 1,
        percentage = 0.2f,
    ),
)
