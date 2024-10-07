/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserRow
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadReceiptBottomSheet(
    state: ReadReceiptBottomSheetState,
    onUserDataClick: (UserId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isVisible = state.selectedEvent != null

    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    if (isVisible) {
        ModalBottomSheet(
            modifier = modifier,
//            modifier = modifier.navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
//                    .imePadding()
            sheetState = sheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    state.eventSink(ReadReceiptBottomSheetEvents.Dismiss)
                }
            }
        ) {
            ReadReceiptBottomSheetContent(
                state = state,
                onUserDataClick = {
                    coroutineScope.launch {
                        sheetState.hide()
                        state.eventSink(ReadReceiptBottomSheetEvents.Dismiss)
                        onUserDataClick.invoke(it)
                    }
                },
            )
            // FIXME remove after https://issuetracker.google.com/issues/275849044
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ReadReceiptBottomSheetContent(
    state: ReadReceiptBottomSheetState,
    onUserDataClick: (UserId) -> Unit,
) {
    LazyColumn {
        item {
            ListItem(
                headlineContent = {
                    Text(text = stringResource(id = CommonStrings.common_seen_by))
                }
            )
        }
        items(
            items = state.selectedEvent?.readReceiptState?.receipts.orEmpty()
        ) {
            val userId = UserId(it.avatarData.id)
            MatrixUserRow(
                isDebugBuild = state.isDebugBuild,
                modifier = Modifier.clickable { onUserDataClick(userId) },
                matrixUser = MatrixUser(
                    userId = userId,
                    displayName = it.avatarData.name,
                    avatarUrl = it.avatarData.url,
                ),
                avatarSize = AvatarSize.ReadReceiptList,
                trailingContent = {
                    Text(
                        text = it.formattedDate,
                        style = ElementTheme.typography.fontBodySmRegular,
                        color = ElementTheme.colors.textSecondary,
                    )
                }
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ReadReceiptBottomSheetPreview(@PreviewParameter(ReadReceiptBottomSheetStateProvider::class) state: ReadReceiptBottomSheetState) = ElementPreview {
    Column {
        ReadReceiptBottomSheetContent(
            state = state,
            onUserDataClick = {},
        )
    }
}
