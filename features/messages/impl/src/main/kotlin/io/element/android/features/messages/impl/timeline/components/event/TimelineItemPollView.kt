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

package io.element.android.features.messages.impl.timeline.components.event

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContentProvider
import io.element.android.libraries.designsystem.preview.DayNightPreviews
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.theme.ElementTheme

@Composable
fun TimelineItemPollView(
    content: TimelineItemPollContent,
    onAnswerSelected: (PollAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(imageVector = Icons.Default.BarChart, contentDescription = null)
            Text(
                text = content.question,
                style = ElementTheme.typography.fontBodyLgMedium
            )
        }

        content.answers.forEach { answer ->
            val votes = content.votes[answer.id].orEmpty()
            PollOptionView(
                answer = answer,
                votes = votes.size,
                progress = if (votes.isNotEmpty()) votes.size.toFloat() / content.votes.flatMap { it.value }.size.toFloat() else 0f,
                onClick = { onAnswerSelected(answer) }
            )
        }
    }
}

@Composable
internal fun PollOptionView(
    answer: PollAnswer,
    votes: Int,
    progress: Float,
    onClick: () -> Unit
) {
    ConstraintLayout(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .selectable(
                selected = false,
                onClick = onClick,
                role = Role.RadioButton,
            )
    ) {
        val (radioButton, answerText, votesText, progressBar) = createRefs()
        RadioButton(
            modifier = Modifier.constrainAs(radioButton) {
                top.linkTo(answerText.top)
                bottom.linkTo(answerText.bottom)
                start.linkTo(parent.start)
                end.linkTo(answerText.start)
            },
            selected = false,
            onClick = null // null recommended for accessibility with screenreaders
        )
        Text(
            modifier = Modifier.constrainAs(answerText) {
                width = Dimension.fillToConstraints
                top.linkTo(parent.top)
                start.linkTo(radioButton.end, margin = 8.dp)
                end.linkTo(votesText.start)
                bottom.linkTo(progressBar.top)
            },
            text = answer.text,
        )
        Text(
            modifier = Modifier.constrainAs(votesText) {
                start.linkTo(answerText.end)
                end.linkTo(parent.end)
                bottom.linkTo(answerText.bottom)
            },
            text = "$votes votes", // Fixme hardcoded string
            style = ElementTheme.typography.fontBodySmRegular,
        )
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .constrainAs(progressBar) {
                    start.linkTo(answerText.start)
                    end.linkTo(votesText.end)
                    top.linkTo(answerText.bottom, margin = 10.dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints

                },
            strokeCap = StrokeCap.Round,
        )
    }
}

@DayNightPreviews
@Composable
internal fun TimelineItemPollViewPreview(@PreviewParameter(TimelineItemPollContentProvider::class) content: TimelineItemPollContent) =
    ElementPreview {
        Box(modifier = Modifier.width(275.dp)) {
            TimelineItemPollView(
                content = content,
                onAnswerSelected = {},
            )
        }
    }


