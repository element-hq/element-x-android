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

@file:OptIn(ExperimentalMaterialApi::class)

package io.element.android.features.messages.actionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.actionlist.model.TimelineItemAction
import io.element.android.features.messages.timeline.createMessageEvent
import io.element.android.features.messages.timeline.model.TimelineItem
import io.element.android.libraries.designsystem.components.VectorIcon
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheetLayout
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun ActionListView(
    state: ActionListState,
    modalBottomSheetState: ModalBottomSheetState,
    onActionSelected: (action: TimelineItemAction, TimelineItem.MessageEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(modalBottomSheetState) {
        snapshotFlow { modalBottomSheetState.currentValue }
            .filter { it == ModalBottomSheetValue.Hidden }
            .collect {
                state.eventSink(ActionListEvents.Clear)
            }
    }

    fun onItemActionClicked(
        itemAction: TimelineItemAction,
        targetItem: TimelineItem.MessageEvent
    ) {
        onActionSelected(itemAction, targetItem)
        coroutineScope.launch {
            modalBottomSheetState.hide()
        }
    }

    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = modalBottomSheetState,
        sheetContent = {
            SheetContent(
                state = state,
                onActionClicked = ::onItemActionClicked,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    )
}

@Composable
private fun SheetContent(
    state: ActionListState,
    modifier: Modifier = Modifier,
    onActionClicked: (TimelineItemAction, TimelineItem.MessageEvent) -> Unit = { _, _ -> },
) {
    when (val target = state.target) {
        is ActionListState.Target.Loading,
        ActionListState.Target.None -> {
            // Crashes if sheetContent size is zero
            Box(modifier = modifier.size(1.dp))
        }
        is ActionListState.Target.Success -> {
            val actions = target.actions
            LazyColumn(
                modifier = modifier.fillMaxWidth()
            ) {
                items(
                    items = actions,
                ) { action ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onActionClicked(action, target.messageEvent)
                        },
                        text = {
                            Text(
                                text = action.title,
                                color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        },
                        icon = {
                            VectorIcon(
                                resourceId = action.icon,
                                tint = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    )
                }
            }
        }
    }
}

open class ActionListStatePreviewParameterProvider : PreviewParameterProvider<ActionListState> {
    override val values: Sequence<ActionListState>
        get() = sequenceOf(
            anActionListState(),
            anActionListState().copy(target = ActionListState.Target.Loading(createMessageEvent())),
            anActionListState().copy(
                target = ActionListState.Target.Success(
                    messageEvent = createMessageEvent(),
                    actions = persistentListOf(
                        TimelineItemAction.Reply,
                        TimelineItemAction.Forward,
                        TimelineItemAction.Copy,
                        TimelineItemAction.Edit,
                        TimelineItemAction.Redact,
                    )
                )
            )
        )
}

@Preview
@Composable
fun SheetContentLightPreview(@PreviewParameter(ActionListStatePreviewParameterProvider::class) state: ActionListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun SheetContentDarkPreview(@PreviewParameter(ActionListStatePreviewParameterProvider::class) state: ActionListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ActionListState) {
    SheetContent(state)
}
