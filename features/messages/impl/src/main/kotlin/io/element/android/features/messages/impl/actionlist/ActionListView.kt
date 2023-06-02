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

package io.element.android.features.messages.impl.actionlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionListView(
    state: ActionListState,
    isVisible: MutableState<Boolean>,
    onActionSelected: (action: TimelineItemAction, TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(isVisible.value) {
        if (!isVisible.value) {
            state.eventSink(ActionListEvents.Clear)
        }
    }

    fun onItemActionClicked(
        itemAction: TimelineItemAction,
        targetItem: TimelineItem.Event
    ) {
        onActionSelected(itemAction, targetItem)
        isVisible.value = false
    }

    if (isVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                isVisible.value = false
            }
        ) {
            SheetContent(
                state = state,
                onActionClicked = ::onItemActionClicked,
                modifier = modifier
                    .padding(bottom = 32.dp)
//                    .navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
//                    .imePadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SheetContent(
    state: ActionListState,
    modifier: Modifier = Modifier,
    onActionClicked: (TimelineItemAction, TimelineItem.Event) -> Unit = { _, _ -> },
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
                            onActionClicked(action, target.event)
                        },
                        text = {
                            Text(
                                text = action.title,
                                color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        },
                        icon = {
                            Icon(
                                resourceId = action.icon,
                                contentDescription = "",
                                tint = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun SheetContentLightPreview(@PreviewParameter(ActionListStateProvider::class) state: ActionListState) =
    ElementPreviewLight { ContentToPreview(state) }

@Preview
@Composable
fun SheetContentDarkPreview(@PreviewParameter(ActionListStateProvider::class) state: ActionListState) =
    ElementPreviewDark { ContentToPreview(state) }

@Composable
private fun ContentToPreview(state: ActionListState) {
    val isVisible = remember { mutableStateOf(true) }
    ActionListView(
        state = state,
        isVisible = isVisible,
        onActionSelected = { _, _ -> }
    )
}
