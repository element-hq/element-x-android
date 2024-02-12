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

package io.element.android.features.roomlist.impl.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.features.roomlist.impl.RoomListState
import io.element.android.features.roomlist.impl.aRoomListState
import io.element.android.features.roomlist.impl.components.RoomSummaryRow
import io.element.android.features.roomlist.impl.contentType
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.copy
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun RoomListSearchResultView(
    state: RoomListState,
    onRoomClicked: (RoomId) -> Unit,
    onRoomLongClicked: (RoomListRoomSummary) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = state.displaySearchResults,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier
                .applyIf(state.displaySearchResults, ifTrue = {
                    // Disable input interaction to underlying views
                    pointerInput(Unit) {}
                })
        ) {
            if (state.displaySearchResults) {
                RoomListSearchResultContent(
                    state = state,
                    onRoomClicked = onRoomClicked,
                    onRoomLongClicked = onRoomLongClicked,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListSearchResultContent(
    state: RoomListState,
    onRoomClicked: (RoomId) -> Unit,
    onRoomLongClicked: (RoomListRoomSummary) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.tertiary
    val strokeWidth = 1.dp
    fun onBackButtonPressed() {
        state.eventSink(RoomListEvents.ToggleSearchResults)
    }

    fun onRoomClicked(room: RoomListRoomSummary) {
        onRoomClicked(room.roomId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = strokeWidth.value
                    )
                },
                navigationIcon = { BackButton(onClick = ::onBackButtonPressed) },
                title = {
                    val filter = state.filter.orEmpty()
                    val focusRequester = FocusRequester()
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = filter,
                        singleLine = true,
                        onValueChange = { state.eventSink(RoomListEvents.UpdateFilter(it)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            if (filter.isNotEmpty()) {
                                IconButton(onClick = {
                                    state.eventSink(RoomListEvents.UpdateFilter(""))
                                }) {
                                    Icon(
                                        imageVector = CompoundIcons.Close(),
                                        contentDescription = stringResource(CommonStrings.action_cancel)
                                    )
                                }
                            }
                        }
                    )

                    LaunchedEffect(state.displaySearchResults) {
                        if (state.displaySearchResults) {
                            focusRequester.requestFocus()
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets.copy(top = 0)
            )
        }
    ) { padding ->
        val lazyListState = rememberLazyListState()
        val visibleRange by remember {
            derivedStateOf {
                val layoutInfo = lazyListState.layoutInfo
                val firstItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
                val size = layoutInfo.visibleItemsInfo.size
                firstItemIndex until firstItemIndex + size
            }
        }
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity
                ): Velocity {
                    state.eventSink(RoomListEvents.UpdateVisibleRange(visibleRange))
                    return super.onPostFling(consumed, available)
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection),
                state = lazyListState,
            ) {
                items(
                    items = state.filteredRoomList,
                    contentType = { room -> room.contentType() },
                ) { room ->
                    RoomSummaryRow(
                        room = room,
                        onClick = ::onRoomClicked,
                        onLongClick = onRoomLongClicked,
                    )
                }
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomListSearchResultContentPreview() = ElementPreview {
    RoomListSearchResultContent(
        state = aRoomListState(),
        onRoomClicked = {},
        onRoomLongClicked = {}
    )
}
