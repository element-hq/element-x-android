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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun PollContentView(
    startEventId: EventId?,
    question: String,
    answerItems: ImmutableList<PollAnswerItem>,
    pollKind: PollKind,
    isPollEnded: Boolean,
    onAnswerSelected: (PollAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .selectableGroup()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PollTitle(title = question)

        PollAnswers(
            startEventId = startEventId,
            answerItems = answerItems,
            onAnswerSelected = onAnswerSelected
        )

        when {
            isPollEnded || pollKind == PollKind.Disclosed -> DisclosedPollBottomNotice(answerItems)
            pollKind == PollKind.Undisclosed -> UndisclosedPollBottomNotice()
        }
    }
}

@Composable
internal fun PollTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            imageVector = Icons.Outlined.Poll,
            contentDescription = null
        )
        Text(
            text = title,
            style = ElementTheme.typography.fontBodyLgMedium
        )
    }
}

@Composable
internal fun PollAnswers(
    startEventId: EventId?,
    answerItems: ImmutableList<PollAnswerItem>,
    onAnswerSelected: (PollAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {

    answerItems.forEach { answerItem ->
        PollAnswerView(
            modifier = modifier,
            startEventId = startEventId,
            answerItem = answerItem,
            onClick = { onAnswerSelected(answerItem.answer) }
        )
    }
}

@Composable
internal fun ColumnScope.DisclosedPollBottomNotice(
    answerItems: ImmutableList<PollAnswerItem>,
    modifier: Modifier = Modifier
) {
    val votesCount = answerItems.sumOf { it.votesCount }
    Text(
        modifier = modifier.align(Alignment.End),
        style = ElementTheme.typography.fontBodyXsRegular,
        color = ElementTheme.colors.textSecondary,
        text = stringResource(CommonStrings.common_poll_total_votes, votesCount),
    )
}

@Composable
fun ColumnScope.UndisclosedPollBottomNotice(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .align(Alignment.Start)
            .padding(start = 34.dp),
        style = ElementTheme.typography.fontBodyXsRegular,
        color = ElementTheme.colors.textSecondary,
        text = stringResource(CommonStrings.common_poll_undisclosed_text),
    )
}

@DayNightPreviews
@Composable
internal fun PollContentUndisclosedPreview() = ElementPreview {
    PollContentView(
        startEventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(isDisclosed = false),
        pollKind = PollKind.Undisclosed,
        isPollEnded = false,
        onAnswerSelected = { },
    )
}

@DayNightPreviews
@Composable
internal fun PollContentDisclosedPreview() = ElementPreview {
    PollContentView(
        startEventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(),
        pollKind = PollKind.Disclosed,
        isPollEnded = false,
        onAnswerSelected = { },
    )
}

@DayNightPreviews
@Composable
internal fun PollContentEndedPreview() = ElementPreview {
    PollContentView(
        startEventId = EventId("\$anEventId"),
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(isEnded = true),
        pollKind = PollKind.Disclosed,
        isPollEnded = false,
        onAnswerSelected = { },
    )
}
