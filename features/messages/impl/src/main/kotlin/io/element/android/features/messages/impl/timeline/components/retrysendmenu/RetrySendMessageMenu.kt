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

package io.element.android.features.messages.impl.timeline.components.retrysendmenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.R
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.coroutines.launch

@Composable
internal fun RetrySendMessageMenu(
    state: RetrySendMenuState,
    modifier: Modifier = Modifier,
) {
    val isVisible = state.selectedEvent != null

    fun onDismiss() {
        state.eventSink(RetrySendMenuEvents.Dismiss)
    }

    fun onRetry() {
        state.eventSink(RetrySendMenuEvents.RetrySend)
    }

    fun onRemoveFailed() {
        state.eventSink(RetrySendMenuEvents.RemoveFailed)
    }

    RetrySendMessageMenuBottomSheet(
        modifier = modifier,
        isVisible = isVisible,
        onRetry = ::onRetry,
        onRemoveFailed = ::onRemoveFailed,
        onDismiss = ::onDismiss
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RetrySendMessageMenuBottomSheet(
    isVisible: Boolean,
    onRetry: () -> Unit,
    onRemoveFailed: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    onDismiss()
                }
            }
        ) {
            RetrySendMenuContents(onRetry = onRetry, onRemoveFailed = onRemoveFailed)
            // FIXME remove after https://issuetracker.google.com/issues/275849044
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.RetrySendMenuContents(
    onRetry: () -> Unit,
    onRemoveFailed: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
) {
    val coroutineScope = rememberCoroutineScope()

    ListItem(headlineContent = {
        Text(
            text = stringResource(R.string.screen_room_retry_send_menu_title),
            style = ElementTheme.typography.fontBodyLgMedium,
        )
    })
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(R.string.screen_room_retry_send_menu_send_again_action),
                style = ElementTheme.typography.fontBodyLgRegular,
            )
        },
        modifier = Modifier.clickable {
            coroutineScope.launch {
                sheetState.hide()
                onRetry()
            }
        }
    )
    ListItem(
        headlineContent = {
            Text(
                text = stringResource(CommonStrings.action_remove),
                style = ElementTheme.typography.fontBodyLgRegular,
            )
        },
        colors = ListItemDefaults.colors(headlineColor = MaterialTheme.colorScheme.error),
        modifier = Modifier.clickable {
            coroutineScope.launch {
                sheetState.hide()
                onRemoveFailed()
            }
        }
    )
}

@Suppress("UNUSED_PARAMETER")
@OptIn(ExperimentalMaterial3Api::class)
@PreviewsDayNight
@Composable
internal fun RetrySendMessageMenuPreview(@PreviewParameter(RetrySendMenuStateProvider::class) state: RetrySendMenuState) = ElementPreview {
    // TODO restore RetrySendMessageMenuBottomSheet once the issue with bottom sheet not being previewable is fixed
    Column {
        RetrySendMenuContents(
            onRetry = {},
            onRemoveFailed = {},
        )
    }
}
