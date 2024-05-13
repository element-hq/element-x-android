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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PollContentView(
    state: PollContentState,
    onAnswerSelected: (pollStartId: EventId, answerId: String) -> Unit,
    onPollEdit: (pollStartId: EventId) -> Unit,
    onPollEnd: (pollStartId: EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    PollContentView(
        eventId = state.eventId,
        question = state.question,
        answerItems = state.answerItems,
        pollKind = state.pollKind,
        isPollEditable = state.isPollEditable,
        isPollEnded = state.isPollEnded,
        isMine = state.isMine,
        onPollEdit = onPollEdit,
        onAnswerSelected = onAnswerSelected,
        onPollEnd = onPollEnd,
        modifier = modifier,
    )
}

@Composable
fun PollContentView(
    eventId: EventId?,
    question: String,
    answerItems: ImmutableList<PollAnswerItem>,
    pollKind: PollKind,
    isPollEditable: Boolean,
    isPollEnded: Boolean,
    isMine: Boolean,
    onAnswerSelected: (pollStartId: EventId, answerId: String) -> Unit,
    onPollEdit: (pollStartId: EventId) -> Unit,
    onPollEnd: (pollStartId: EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val votesCount = remember(answerItems) { answerItems.sumOf { it.votesCount } }

    fun onAnswerSelected(pollAnswer: PollAnswer) {
        eventId?.let { onAnswerSelected(it, pollAnswer.id) }
    }

    fun onPollEdit() {
        eventId?.let { onPollEdit(it) }
    }

    fun onPollEnd() {
        eventId?.let { onPollEnd(it) }
    }

    var showConfirmation: Boolean by remember { mutableStateOf(false) }

    if (showConfirmation) {
        ConfirmationDialog(
            content = stringResource(id = CommonStrings.common_poll_end_confirmation),
            onSubmitClicked = {
                onPollEnd()
                showConfirmation = false
            },
            onDismiss = { showConfirmation = false },
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PollTitle(title = question, isPollEnded = isPollEnded)

        PollAnswers(answerItems = answerItems, onAnswerSelected = ::onAnswerSelected)

        if (isPollEnded || pollKind == PollKind.Disclosed) {
            DisclosedPollBottomNotice(votesCount = votesCount)
        } else {
            UndisclosedPollBottomNotice()
        }

        if (isMine) {
            CreatorView(
                isPollEnded = isPollEnded,
                isPollEditable = isPollEditable,
                onPollEdit = ::onPollEdit,
                onPollEnd = { showConfirmation = true },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PollTitle(
    title: String,
    isPollEnded: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (isPollEnded) {
            Icon(
                imageVector = CompoundIcons.PollsEnd(),
                contentDescription = stringResource(id = CommonStrings.a11y_poll_end),
                modifier = Modifier.size(22.dp)
            )
        } else {
            Icon(
                imageVector = CompoundIcons.Polls(),
                contentDescription = stringResource(id = CommonStrings.a11y_poll),
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyLgMedium
        )
    }
}

@Composable
private fun PollAnswers(
    answerItems: ImmutableList<PollAnswerItem>,
    onAnswerSelected: (PollAnswer) -> Unit,
) {
    Column(
        modifier = Modifier.selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        answerItems.forEach {
            PollAnswerView(
                answerItem = it,
                modifier = Modifier
                    .selectable(
                        selected = it.isSelected,
                        enabled = it.isEnabled,
                        onClick = { onAnswerSelected(it.answer) },
                        role = Role.RadioButton,
                    ),
            )
        }
    }
}

@Composable
private fun ColumnScope.DisclosedPollBottomNotice(
    votesCount: Int,
) {
    Text(
        modifier = Modifier.align(Alignment.End),
        style = ElementTheme.typography.fontBodyXsRegular,
        color = ElementTheme.colors.textSecondary,
        text = stringResource(CommonStrings.common_poll_total_votes, votesCount),
    )
}

@Composable
private fun ColumnScope.UndisclosedPollBottomNotice() {
    Text(
        modifier = Modifier
            .align(Alignment.Start)
            .padding(start = 34.dp),
        style = ElementTheme.typography.fontBodyXsRegular,
        color = ElementTheme.colors.textSecondary,
        text = stringResource(CommonStrings.common_poll_undisclosed_text),
    )
}

@Composable
private fun CreatorView(
    isPollEnded: Boolean,
    isPollEditable: Boolean,
    onPollEdit: () -> Unit,
    onPollEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isPollEditable ->
            Button(
                text = stringResource(id = CommonStrings.action_edit_poll),
                onClick = onPollEdit,
                modifier = modifier,
            )
        !isPollEnded ->
            Button(
                text = stringResource(id = CommonStrings.action_end_poll),
                onClick = onPollEnd,
                modifier = modifier,
            )
    }
}

@PreviewsDayNight
@Composable
internal fun PollContentViewUndisclosedPreview() = ElementPreview {
    PollContentView(
        eventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(showVotes = false),
        pollKind = PollKind.Undisclosed,
        isPollEnded = false,
        isPollEditable = false,
        isMine = false,
        onAnswerSelected = { _, _ -> },
        onPollEdit = {},
        onPollEnd = {},
    )
}

@PreviewsDayNight
@Composable
internal fun PollContentViewDisclosedPreview() = ElementPreview {
    PollContentView(
        eventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(),
        pollKind = PollKind.Disclosed,
        isPollEnded = false,
        isPollEditable = false,
        isMine = false,
        onAnswerSelected = { _, _ -> },
        onPollEdit = {},
        onPollEnd = {},
    )
}

@PreviewsDayNight
@Composable
internal fun PollContentViewEndedPreview() = ElementPreview {
    PollContentView(
        eventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(isEnded = true),
        pollKind = PollKind.Disclosed,
        isPollEnded = true,
        isPollEditable = false,
        isMine = false,
        onAnswerSelected = { _, _ -> },
        onPollEdit = {},
        onPollEnd = {},
    )
}

@PreviewsDayNight
@Composable
internal fun PollContentViewCreatorEditablePreview() = ElementPreview {
    PollContentView(
        eventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(hasVotes = false, isEnded = false),
        pollKind = PollKind.Disclosed,
        isPollEnded = false,
        isPollEditable = true,
        isMine = true,
        onAnswerSelected = { _, _ -> },
        onPollEdit = {},
        onPollEnd = {},
    )
}

@PreviewsDayNight
@Composable
internal fun PollContentViewCreatorPreview() = ElementPreview {
    PollContentView(
        eventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(isEnded = false),
        pollKind = PollKind.Disclosed,
        isPollEnded = false,
        isPollEditable = false,
        isMine = true,
        onAnswerSelected = { _, _ -> },
        onPollEdit = {},
        onPollEnd = {},
    )
}

@PreviewsDayNight
@Composable
internal fun PollContentViewCreatorEndedPreview() = ElementPreview {
    PollContentView(
        eventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(isEnded = true),
        pollKind = PollKind.Disclosed,
        isPollEnded = true,
        isPollEditable = false,
        isMine = true,
        onAnswerSelected = { _, _ -> },
        onPollEdit = {},
        onPollEnd = {},
    )
}
