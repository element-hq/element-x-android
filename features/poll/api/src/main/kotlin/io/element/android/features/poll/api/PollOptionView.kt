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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.Visibility
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.RadioButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.poll.PollAnswer
import io.element.android.libraries.theme.ElementTheme

@Composable
fun PollOptionView(
    showResults: Boolean,
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
                visibility = if (showResults) Visibility.Visible else Visibility.Gone
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
                    visibility = if (showResults) Visibility.Visible else Visibility.Gone

                },
            strokeCap = StrokeCap.Round,
        )
    }
}
