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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ActivePollContentView(
    question: String,
    answerItems: ImmutableList<PollAnswerItem>,
    pollKind: PollKind,
    onAnswerSelected: (PollAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    val showResults = answerItems.any { it.isSelected }
    Column(
        modifier = modifier
            .selectableGroup()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(imageVector = Icons.Default.BarChart, contentDescription = null)
            Text(
                text = question,
                style = ElementTheme.typography.fontBodyLgMedium
            )
        }

        answerItems.forEach { answerItem ->
            PollAnswerView(
                answerItem = answerItem,
                onClick = { onAnswerSelected(answerItem.answer) }
            )
        }

        val votesCount = answerItems.sumOf { it.votesCount }
        when {
            pollKind == PollKind.Undisclosed -> {
                Text(
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 32.dp),
                    style = ElementTheme.typography.fontBodyXsRegular,
                    color = ElementTheme.colors.textSecondary,
                    text = stringResource(CommonStrings.common_poll_undisclosed_text),
                )
            }
            showResults -> {
                Text(
                    modifier = Modifier.align(Alignment.End),
                    style = ElementTheme.typography.fontBodyXsRegular,
                    color = ElementTheme.colors.textSecondary,
                    text = stringResource(CommonStrings.common_poll_total_votes, votesCount),
                )
            }
        }
    }
}

@DayNightPreviews
@Composable
internal fun ActivePollContentNoResultsPreview() = ElementPreview {
    ActivePollContentView(
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(isDisclosed = false),
        pollKind = PollKind.Undisclosed,
        onAnswerSelected = { },
    )
}

@DayNightPreviews
@Composable
internal fun ActivePollContentWithResultsPreview() = ElementPreview {
    ActivePollContentView(
        question = "What type of food should we have at the party?",
        answerItems = aPollAnswerItemList(),
        pollKind = PollKind.Disclosed,
        onAnswerSelected = { },
    )
}
