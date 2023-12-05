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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.poll.api.pollcontent.PollContentView
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.aliasScreenTitle
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.EventId

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
            itemsIndexed(state.pollItems) { index, pollHistoryItem ->
                PollHistoryItemRow(
                    pollHistoryItem = pollHistoryItem,
                    onAnswerSelected = fun(pollStartId: EventId, answerId: String) {
                        state.eventSink(PollHistoryEvents.PollAnswerSelected(pollStartId, answerId))
                    },
                    onPollEdit = {
                        state.eventSink(PollHistoryEvents.EditPoll)
                    },
                    onPollEnd = {
                        state.eventSink(PollHistoryEvents.PollEndClicked(it))
                    },
                )
                if (index != state.pollItems.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun PollHistoryItemRow(
    pollHistoryItem: PollHistoryItem,
    onAnswerSelected: (pollStartId: EventId, answerId: String) -> Unit,
    onPollEdit: (pollStartId: EventId) -> Unit,
    onPollEnd: (pollStartId: EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (pollHistoryItem) {
        is PollHistoryItem.PollContent -> {
            PollContentItemRow(
                pollContentItem = pollHistoryItem,
                onAnswerSelected = onAnswerSelected,
                onPollEdit = onPollEdit,
                onPollEnd = onPollEnd,
                modifier = modifier.padding(
                    horizontal = 16.dp,
                    vertical = 24.dp
                ),
            )
        }
    }
}

@Composable
private fun PollContentItemRow(
    pollContentItem: PollHistoryItem.PollContent,
    onAnswerSelected: (pollStartId: EventId, answerId: String) -> Unit,
    onPollEdit: (pollStartId: EventId) -> Unit,
    onPollEnd: (pollStartId: EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = pollContentItem.formattedDate,
            color = MaterialTheme.colorScheme.secondary,
            style = ElementTheme.typography.fontBodySmRegular,
        )
        Spacer(modifier = Modifier.height(4.dp))
        PollContentView(
            state = pollContentItem.state,
            onAnswerSelected = onAnswerSelected,
            onPollEdit = onPollEdit,
            onPollEnd = onPollEnd,
        )
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
