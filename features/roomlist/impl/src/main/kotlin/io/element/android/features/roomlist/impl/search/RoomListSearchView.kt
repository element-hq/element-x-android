/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.roomlist.impl.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.RoomListEvents
import io.element.android.features.roomlist.impl.components.RoomSummaryRow
import io.element.android.features.roomlist.impl.contentType
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.button.SuperButton
import io.element.android.libraries.designsystem.modifiers.applyIf
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ButtonSize
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.copy
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
internal fun RoomListSearchView(
    state: RoomListSearchState,
    eventSink: (RoomListEvents) -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onRoomDirectorySearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.isSearchActive) {
        state.eventSink(RoomListSearchEvents.ToggleSearchVisibility)
    }

    AnimatedVisibility(
        visible = state.isSearchActive,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier
                .applyIf(
                    condition = state.isSearchActive,
                    ifTrue = {
                        // Disable input interaction to underlying views
                        pointerInput(Unit) {}
                    }
                )
        ) {
            if (state.isSearchActive) {
                RoomListSearchContent(
                    state = state,
                    onRoomClick = onRoomClick,
                    eventSink = eventSink,
                    onRoomDirectorySearchClick = onRoomDirectorySearchClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoomListSearchContent(
    state: RoomListSearchState,
    eventSink: (RoomListEvents) -> Unit,
    onRoomClick: (RoomId) -> Unit,
    onRoomDirectorySearchClick: () -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.tertiary
    val strokeWidth = 1.dp
    fun onBackButtonClick() {
        state.eventSink(RoomListSearchEvents.ToggleSearchVisibility)
    }

    fun onRoomClick(room: RoomListRoomSummary) {
        onRoomClick(room.roomId)
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
                navigationIcon = { BackButton(onClick = ::onBackButtonClick) },
                title = {
                    val filter = state.query
                    val focusRequester = FocusRequester()
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = filter,
                        singleLine = true,
                        onValueChange = { state.eventSink(RoomListSearchEvents.QueryChanged(it)) },
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
                                    state.eventSink(RoomListSearchEvents.ClearQuery)
                                }) {
                                    Icon(
                                        imageVector = CompoundIcons.Close(),
                                        contentDescription = stringResource(CommonStrings.action_cancel)
                                    )
                                }
                            }
                        }
                    )

                    LaunchedEffect(state.isSearchActive) {
                        if (state.isSearchActive) {
                            focusRequester.requestFocus()
                        }
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets.copy(top = 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            if (state.displayRoomDirectorySearch) {
                RoomDirectorySearchButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    onClick = onRoomDirectorySearchClick
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(
                    items = state.results,
                    contentType = { room -> room.contentType() },
                ) { room ->
                    RoomSummaryRow(
                        isDebugBuild = state.isDebugBuild,
                        room = room,
                        onClick = ::onRoomClick,
                        eventSink = eventSink,
                    )
                }
            }
        }
    }
}

@Composable
private fun RoomDirectorySearchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuperButton(
        onClick = onClick,
        modifier = modifier,
        buttonSize = ButtonSize.Large,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = CompoundIcons.ListBulleted(),
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.screen_roomlist_room_directory_button_title),
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RoomListSearchContentPreview(@PreviewParameter(RoomListSearchStateProvider::class) state: RoomListSearchState) = ElementPreview {
    RoomListSearchContent(
        state = state,
        onRoomClick = {},
        eventSink = {},
        onRoomDirectorySearchClick = {},
    )
}
