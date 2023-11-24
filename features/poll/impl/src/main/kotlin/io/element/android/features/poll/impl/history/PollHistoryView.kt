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

package io.element.android.features.poll.impl.history

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.poll.api.PollAnswerItem
import io.element.android.features.poll.api.PollContentView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.poll.PollAnswer
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollHistoryView(
    state: PollHistoryState,
    modifier: Modifier = Modifier,
    goBack: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Polls", // TODO Polls: Localazy
                        style = ElementTheme.typography.aliasScreenTitle,
                    )
                },
                navigationIcon = {
                    BackButton(onClick = goBack)
                },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            reverseLayout = false,
        ) {
            if (state.paginationState.isBackPaginating) item {
                CircularProgressIndicator()
            }
            items(state.matrixTimelineItems) { pollContent ->
                PollContentView(
                    eventId = null,
                    question = pollContent.question,
                    answerItems = pollContent.answers.map {
                        PollAnswerItem(
                            answer = PollAnswer(
                                id = it.id,
                                text = it.text,
                            ),
                            isSelected = false,
                            isEnabled = false,
                            isWinner = false,
                            isDisclosed = false,
                            votesCount = 9393,
                            percentage = 4.5f,
                        )
                    }.toImmutableList(),
                    pollKind = pollContent.kind,
                    isPollEditable = false,
                    isPollEnded = false,
                    isMine = false,
                    onAnswerSelected = { _, _ -> },
                    onPollEdit = {},
                    onPollEnd = {},
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun PollHistoryViewPreview(
    @PreviewParameter(PollHistoryStateProvider::class) state: PollHistoryState
) = ElementPreview {
    PollHistoryView(
        state = state,
        goBack = {},
    )
}
