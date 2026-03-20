/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roommessagesearch.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SearchField
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TextButton
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.ui.strings.CommonStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomMessageSearchView(
    state: RoomMessageSearchState,
    onBackClick: () -> Unit,
    onSearchResultClick: (EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchFieldState = remember { TextFieldState() }

    if (!state.isEncryptedRoom) {
        LaunchedEffect(Unit) {
            snapshotFlow { searchFieldState.text.toString() }
                .collect { text ->
                    state.eventSink(RoomMessageSearchEvents.UpdateQuery(text))
                }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(CommonStrings.action_search)) },
                navigationIcon = { BackButton(onClick = onBackClick) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.isEncryptedRoom) {
                EncryptedRoomBanner()
            } else {
                SearchField(
                    state = searchFieldState,
                    placeholder = stringResource(CommonStrings.action_search),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                SearchContent(
                    state = state,
                    onSearchResultClick = onSearchResultClick,
                )
            }
        }
    }
}

@Composable
private fun EncryptedRoomBanner(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = CompoundIcons.Lock(),
            contentDescription = null,
            tint = ElementTheme.colors.iconSecondary,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.screen_room_message_search_encrypted_room),
            style = ElementTheme.typography.fontBodyMdRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun SearchContent(
    state: RoomMessageSearchState,
    onSearchResultClick: (EventId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val results = state.searchResults) {
        is AsyncData.Uninitialized -> {
            EmptyState(
                message = stringResource(R.string.screen_room_message_search_type_to_search),
                modifier = modifier,
            )
        }
        is AsyncData.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
        is AsyncData.Failure -> {
            ErrorState(
                onRetry = { state.eventSink(RoomMessageSearchEvents.RetrySearch) },
                modifier = modifier,
            )
        }
        is AsyncData.Success -> {
            Column(modifier = modifier) {
                // Result count banner (like Element Desktop)
                ResultCountBanner(
                    count = results.data.count,
                    query = state.query,
                )
                HorizontalDivider()
                if (results.data.items.isEmpty()) {
                    EmptyState(
                        message = stringResource(CommonStrings.common_no_results),
                    )
                } else {
                    SearchResultsList(
                        results = results.data,
                        highlights = results.data.items.firstOrNull()?.highlights ?: emptyList(),
                        onSearchResultClick = onSearchResultClick,
                        onLoadMore = { state.eventSink(RoomMessageSearchEvents.LoadMore) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCountBanner(
    count: Long?,
    query: String,
    modifier: Modifier = Modifier,
) {
    if (count != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = CompoundIcons.Search(),
                contentDescription = null,
                tint = ElementTheme.colors.iconSecondary,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("$count results found for \"")
                    val start = length
                    append(query)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                    append("\"")
                },
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SearchResultsList(
    results: SearchResults,
    highlights: List<String>,
    onSearchResultClick: (EventId) -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem >= totalItems - 3 && results.hasMore
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = results.items,
            key = { it.eventId.value },
        ) { item ->
            SearchResultItem(
                item = item,
                highlights = highlights,
                onClick = { onSearchResultClick(item.eventId) },
            )
        }
        if (results.hasMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    item: SearchResultItemState,
    highlights: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Avatar(avatarData = item.senderAvatar, avatarType = AvatarType.User)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.senderName ?: item.senderId.value,
                    style = ElementTheme.typography.fontBodyMdMedium,
                    color = ElementTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.formattedDate,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
            Spacer(Modifier.height(2.dp))
            HighlightedText(
                text = item.body,
                highlights = highlights,
            )
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    highlights: List<String>,
    modifier: Modifier = Modifier,
) {
    val highlightColor = ElementTheme.colors.textActionAccent.copy(alpha = 0.2f)
    val annotatedString = remember(text, highlights, highlightColor) {
        buildAnnotatedString {
            append(text)
            val lowerText = text.lowercase()
            for (highlight in highlights) {
                val lowerHighlight = highlight.lowercase()
                var startIndex = 0
                while (true) {
                    val index = lowerText.indexOf(lowerHighlight, startIndex)
                    if (index == -1) break
                    addStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            background = highlightColor,
                        ),
                        start = index,
                        end = index + highlight.length,
                    )
                    startIndex = index + highlight.length
                }
            }
        }
    }
    Text(
        text = annotatedString,
        style = ElementTheme.typography.fontBodyMdRegular,
        color = ElementTheme.colors.textSecondary,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
        )
    }
}

@Composable
private fun ErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(CommonStrings.common_something_went_wrong),
            style = ElementTheme.typography.fontBodyLgRegular,
            color = ElementTheme.colors.textSecondary,
        )
        Spacer(Modifier.height(16.dp))
        TextButton(
            text = stringResource(CommonStrings.action_retry),
            onClick = onRetry,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun RoomMessageSearchViewPreview(
    @PreviewParameter(RoomMessageSearchStateProvider::class) state: RoomMessageSearchState,
) = ElementPreview {
    RoomMessageSearchView(
        state = state,
        onBackClick = {},
        onSearchResultClick = {},
    )
}
