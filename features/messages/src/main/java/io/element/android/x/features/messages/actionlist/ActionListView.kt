@file:OptIn(ExperimentalMaterialApi::class)

package io.element.android.x.features.messages.actionlist

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
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.x.designsystem.components.VectorIcon
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun ActionListView(
    state: ActionListState,
    modalBottomSheetState: ModalBottomSheetState,
    onActionSelected: (action: TimelineItemAction, MessagesTimelineItemState.MessageEvent) -> Unit,
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
        targetItem: MessagesTimelineItemState.MessageEvent
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
    ) {}
}

@Composable
private fun SheetContent(
    state: ActionListState,
    modifier: Modifier = Modifier,
    onActionClicked: (TimelineItemAction, MessagesTimelineItemState.MessageEvent) -> Unit = { _, _ -> },
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
                                color = if (action.destructive) MaterialTheme.colors.error else Color.Unspecified,
                            )
                        },
                        icon = {
                            VectorIcon(
                                resourceId = action.icon,
                                tint = if (action.destructive) MaterialTheme.colors.error else LocalContentColor.current,
                            )
                        }
                    )
                }
            }
        }
    }
}

