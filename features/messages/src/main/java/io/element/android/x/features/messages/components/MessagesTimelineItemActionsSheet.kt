@file:OptIn(ExperimentalMaterialApi::class)

package io.element.android.x.features.messages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.element.android.x.designsystem.components.VectorIcon
import io.element.android.x.features.messages.model.MessagesItemAction
import io.element.android.x.features.messages.model.MessagesItemActionsSheetState

@Composable
fun TimelineItemActionsScreen(
    sheetState: ModalBottomSheetState,
    actionsSheetState: MessagesItemActionsSheetState?,
    onActionClicked: (MessagesItemAction) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheetLayout(
        modifier = modifier,
        sheetState = sheetState,
        sheetContent = {
            SheetContent(
                actionsSheetState = actionsSheetState,
                onActionClicked = onActionClicked
            )
        }
    ) {}

}

@Composable
private fun SheetContent(
    actionsSheetState: MessagesItemActionsSheetState?,
    onActionClicked: (MessagesItemAction) -> Unit,
) {
    if (actionsSheetState == null || actionsSheetState.actions.isEmpty()) {
        // Crashes if sheetContent size is zero
        Box(modifier = Modifier.size(1.dp))
        return
    }
    LazyColumn {
        items(actionsSheetState.actions) {
            ListItem(
                modifier = Modifier.clickable {
                    onActionClicked(it)
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