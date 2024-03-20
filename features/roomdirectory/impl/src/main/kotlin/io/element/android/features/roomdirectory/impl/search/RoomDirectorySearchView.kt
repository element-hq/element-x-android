/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomdirectory.impl.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.roomdirectory.impl.search.model.RoomDirectorySearchResult
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TextField
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RoomDirectorySearchView(
    state: RoomDirectorySearchState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {

    fun onQueryChanged(query: String) {
        state.eventSink(RoomDirectorySearchEvents.Search(query))
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            RoomDirectorySearchTopBar(
                query = state.query,
                onQueryChanged = ::onQueryChanged,
                onBackPressed = onBackPressed,
            )
        },
        content = { padding ->
            RoomDirectorySearchContent(
                state = state,
                onResultClicked = { roomId ->
                    state.eventSink(RoomDirectorySearchEvents.JoinRoom(roomId))
                },
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding)
            )
        }
    )
}

@Composable
private fun RoomDirectorySearchContent(
    state: RoomDirectorySearchState,
    onResultClicked: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(state.results) { result ->
            RoomDirectorySearchResultRow(
                result = result,
                onClick = onResultClicked,
            )
        }
    }
}

@Composable
private fun RoomDirectorySearchResultRow(
    result: RoomDirectorySearchResult,
    onClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(result.roomId) }
            .padding(
                top = 12.dp,
                bottom = 12.dp,
                start = 16.dp,
            )
            .height(IntrinsicSize.Min),
    ) {
        Avatar(
            avatarData = result.avatarData,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = result.name,
                maxLines = 1,
                style = ElementTheme.typography.fontBodyLgRegular,
                color = ElementTheme.colors.textPrimary,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = result.description,
                maxLines = 1,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (result.canBeJoined) {
            CompositionLocalProvider(LocalContentColor provides ElementTheme.colors.textSuccessPrimary) {
                TextButton(
                    text = stringResource(id = CommonStrings.action_join),
                    onClick = { onClick(result.roomId) },
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 4.dp, end = 12.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RoomDirectorySearchTopBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = ElementTheme.colors.borderInteractivePrimary
    val borderStroke = 1.dp
    TopAppBar(
        modifier = modifier.drawBehind {
            drawLine(
                color = borderColor,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = borderStroke.value
            )
        },
        title = {
            val focusRequester = FocusRequester()
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = query,
                singleLine = true,
                onValueChange = onQueryChanged,
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
                    if (query.isNotEmpty()) {
                        IconButton(onClick = {
                            onQueryChanged("")
                        }) {
                            Icon(
                                imageVector = CompoundIcons.Close(),
                                contentDescription = stringResource(CommonStrings.action_cancel),
                            )
                        }
                    }
                }
            )
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        navigationIcon = { BackButton(onClick = onBackPressed) },
    )
}

@PreviewsDayNight
@Composable
fun RoomDirectorySearchViewLightPreview(@PreviewParameter(RoomDirectorySearchStateProvider::class) state: RoomDirectorySearchState) = ElementPreview {
    RoomDirectorySearchView(
        state = state,
        onBackPressed = {},
    )
}
