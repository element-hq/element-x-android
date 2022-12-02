@file:OptIn(ExperimentalMaterialApi::class)

package io.element.android.x.features.messages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.compose.collectAsState
import io.element.android.x.designsystem.components.VectorIcon
import io.element.android.x.features.messages.MessagesViewModel
import io.element.android.x.features.messages.model.MessagesItemAction
import io.element.android.x.features.messages.model.MessagesItemActionsSheetState
import io.element.android.x.features.messages.model.MessagesTimelineItemState
import io.element.android.x.features.messages.model.MessagesViewState
import io.element.android.x.features.messages.model.content.MessagesTimelineItemTextBasedContent
import io.element.android.x.features.messages.textcomposer.MessageComposerViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Composable
fun TimelineItemActionsScreen(
    viewModel: MessagesViewModel,
    composerViewModel: MessageComposerViewModel,
    modalBottomSheetState: ModalBottomSheetState,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(modalBottomSheetState) {
        snapshotFlow { modalBottomSheetState.currentValue }
            .filter { it == ModalBottomSheetValue.Hidden }
            .collect {
                viewModel.computeActionsSheetState(null)
            }
    }

    val itemActionsSheetState by viewModel.collectAsState(MessagesViewState::itemActionsSheetState)

    fun onItemActionClicked(
        itemAction: MessagesItemAction,
        targetItem: MessagesTimelineItemState.MessageEvent
    ) {
        viewModel.handleItemAction(itemAction, targetItem)
        coroutineScope.launch {
            val targetEvent = viewModel.getTargetEvent()
            when (itemAction) {
                is MessagesItemAction.Edit -> {
                    // Entering Edit mode, update the text in the composer.
                    val newComposerText =
                        (targetEvent?.content as? MessagesTimelineItemTextBasedContent)?.body.orEmpty()
                    composerViewModel.updateText(newComposerText)
                }
                else -> Unit
            }
            modalBottomSheetState.hide()
        }
    }


    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = modalBottomSheetState,
        sheetContent = {
            SheetContent(
                actionsSheetState = itemActionsSheetState(),
                onActionClicked = ::onItemActionClicked,
                modifier = Modifier.navigationBarsPadding().imePadding()
            )
        }
    ) {}

}

@Composable
private fun SheetContent(
    actionsSheetState: MessagesItemActionsSheetState?,
    onActionClicked: (MessagesItemAction, MessagesTimelineItemState.MessageEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    if (actionsSheetState == null || actionsSheetState.actions.isEmpty()) {
        // Crashes if sheetContent size is zero
        Box(modifier = modifier.size(1.dp))
        return
    }
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
    ) {
        items(actionsSheetState.actions) {
            ListItem(
                modifier = Modifier.clickable {
                    onActionClicked(it, actionsSheetState.targetItem)
                },
                text = {
                    Text(
                        text = it.title,
                        color = if (it.destructive) MaterialTheme.colors.error else Color.Unspecified,
                    )
                },
                icon = {
                    VectorIcon(
                        resourceId = it.icon,
                        tint = if (it.destructive) MaterialTheme.colors.error else LocalContentColor.current,
                    )
                }
            )
        }
    }
}