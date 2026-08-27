/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R
import io.element.android.features.home.impl.model.RoomListRoomSummary
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.modifiers.niceClickable
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.roundToPx
import io.element.android.libraries.designsystem.theme.components.FilledTextField
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.LinearProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.SegmentedButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.OnVisibleRangeChangeEffect
import io.element.android.libraries.designsystem.utils.lazyColumnContentPadding
import io.element.android.libraries.designsystem.utils.scaffoldScrollableContentInsets
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.model.getAvatarData
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun GlobalSearchView(
    state: GlobalSearchState,
    onSelectSearchResult: (RoomId, EventId?) -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(enabled = state.isSearchActive) {
        state.eventSink(GlobalSearchEvent.ToggleSearchVisibility)
    }

    AnimatedVisibility(
        visible = state.isSearchActive,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(modifier = modifier) {
            GlobalSearchContent(
                state = state,
                onBackButtonClick = {
                    state.eventSink(GlobalSearchEvent.ToggleSearchVisibility)
                },
                onSelectSearchResult = onSelectSearchResult,
            )
        }
    }
}

@Composable
private fun GlobalSearchContent(
    state: GlobalSearchState,
    onBackButtonClick: () -> Unit,
    onSelectSearchResult: (RoomId, EventId?) -> Unit,
) {
    val borderColor = MaterialTheme.colorScheme.tertiary
    val strokeWidth = 1.dp

    val drawBottomLineModifier = Modifier.drawBehind {
        drawLine(
            color = borderColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = strokeWidth.value
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { BackButton(onClick = onBackButtonClick) },
                title = {
                    // The stateSaver will keep the selection state when returning to this UI
                    val focusRequester = remember { FocusRequester() }
                    val searchLabel = stringResource(CommonStrings.action_search)
                    FilledTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .semantics { contentDescription = searchLabel },
                        state = state.queryState,
                        lineLimits = TextFieldLineLimits.SingleLine,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorIndicatorColor = Color.Transparent,
                        ),
                        trailingIcon = if (state.queryState.text.isNotEmpty()) {
                            @Composable {
                                IconButton(onClick = { state.eventSink(GlobalSearchEvent.ClearQuery) }) {
                                    Icon(
                                        imageVector = CompoundIcons.Close(),
                                        contentDescription = stringResource(CommonStrings.a11y_clear_search_field)
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    )

                    LaunchedEffect(Unit) {
                        if (!focusRequester.restoreFocusedChild()) {
                            focusRequester.requestFocus()
                        }
                        focusRequester.saveFocusedChild()
                    }
                }
            )
        },
        contentWindowInsets = scaffoldScrollableContentInsets,
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = drawBottomLineModifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
            ) {
                SegmentedButton(
                    index = 0,
                    count = 2,
                    selected = state.currentTarget == GlobalSearchTarget.ROOMS,
                    onClick = { state.eventSink(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.ROOMS)) },
                    text = stringResource(R.string.search_section_chats),
                )

                SegmentedButton(
                    index = 1,
                    count = 2,
                    selected = state.currentTarget == GlobalSearchTarget.MESSAGES,
                    onClick = { state.eventSink(GlobalSearchEvent.UpdateTarget(GlobalSearchTarget.MESSAGES)) },
                    text = stringResource(R.string.search_section_messages),
                )
            }

            val lazyListState = rememberLazyListState()
            OnVisibleRangeChangeEffect(lazyListState) { visibleRange ->
                state.eventSink(GlobalSearchEvent.UpdateVisibleRange(visibleRange))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = lazyColumnContentPadding,
                state = lazyListState,
            ) {
                val results = state.results.dataOrNull()
                when {
                    state.results.isUninitialized() -> startSearching()
                    state.results.isLoading() -> loading()
                    results?.isEmpty() == true -> emptySearchResults(query = state.queryState.text.toString())
                    results is GlobalSearchResults.RoomListResults -> roomListResults(
                        results = results.results,
                        onRoomClick = { roomId -> onSelectSearchResult(roomId, null) },
                    )
                    results is GlobalSearchResults.MessageSearchResults -> messageSearchResults(
                        results = results.results,
                        onSearchResultSelected = onSelectSearchResult,
                    )
                }
            }
        }
    }
}

private fun LazyListScope.startSearching() {
    item {
        IconTitleSubtitleMolecule(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            title = stringResource(R.string.start_searching_title),
            subTitle = stringResource(R.string.start_searching_subtitle),
            iconStyle = BigIcon.Style.Default(CompoundIcons.Search()),
        )
    }
}

private fun LazyListScope.loading() {
    item {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    }
}

private fun LazyListScope.emptySearchResults(query: String) {
    item {
        IconTitleSubtitleMolecule(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            title = stringResource(R.string.search_no_results_title),
            subTitle = stringResource(R.string.search_no_results_subtitle, query),
            iconStyle = BigIcon.Style.Default(CompoundIcons.Search()),
        )
    }
}

private fun LazyListScope.roomListResults(
    results: ImmutableList<RoomListRoomSummary>,
    onRoomClick: (RoomId) -> Unit,
) {
    itemsIndexed(
        items = results,
        key = { _, roomSummary -> roomSummary.roomId },
    ) { index, result ->
        Row(
            modifier = Modifier
                .niceClickable(onClick = { onRoomClick(result.roomId) })
                .fillMaxWidth()
                .addSeparatorLine(color = ElementTheme.colors.separatorSecondary, isLastItem = index == results.lastIndex)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                avatarData = result.avatarData.copy(size = AvatarSize.SearchRoomListItem),
                avatarType = AvatarType.Room(
                    heroes = result.heroes.map { it.copy(size = AvatarSize.SearchRoomListItem) }.toImmutableList(),
                    isTombstoned = result.isTombstoned,
                ),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = result.name ?: result.roomId.value,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodyLgRegular,
                )

                if (result.isDm) {
                    val userId = result.heroes.firstOrNull()?.id
                    userId?.let {
                        Text(
                            text = it,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = ElementTheme.typography.fontBodyMdRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    }
                } else {
                    result.canonicalAlias?.let {
                        Text(
                            text = it.value,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = ElementTheme.typography.fontBodyMdRegular,
                            color = ElementTheme.colors.textSecondary,
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.messageSearchResults(
    results: ImmutableList<MessageSearchResultItem>,
    onSearchResultSelected: (RoomId, EventId?) -> Unit,
) {
    if (results.isNotEmpty()) {
        item {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = stringResource(R.string.search_from_your_messages_header),
                style = ElementTheme.typography.fontBodyMdMedium,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
    itemsIndexed(
        items = results,
        contentType = { _, messageSearchResult -> messageSearchResult::class.java },
        key = { _, messageSearchResult -> messageSearchResult.eventId },
    ) { index, result ->
        val clickableModifier = Modifier.niceClickable(onClick = { onSearchResultSelected(result.roomId, result.eventId) })
        when (result) {
            is MessageSearchResultItem.Message -> TextMessageSearchResultItemView(
                modifier = clickableModifier.addSeparatorLine(color = ElementTheme.colors.separatorSecondary, isLastItem = index == results.lastIndex),
                avatarData = result.roomInfo.getAvatarData(AvatarSize.RoomListItem),
                avatarType = AvatarType.Room(
                    heroes = result.roomInfo.heroes.map { it.getAvatarData(AvatarSize.RoomListItem) }.toImmutableList(),
                    isTombstoned = result.roomInfo.successorRoom != null
                ),
                roomName = result.roomInfo.name ?: result.roomInfo.id.value,
                body = result.body,
                formattedTimestamp = result.formattedTimestamp,
            )
            is MessageSearchResultItem.Media -> {
                MediaMessageSearchResultItemView(
                    modifier = clickableModifier.addSeparatorLine(color = ElementTheme.colors.separatorSecondary, isLastItem = index == results.lastIndex),
                    avatarData = result.roomInfo.getAvatarData(AvatarSize.RoomListItem),
                    avatarType = AvatarType.Room(
                        heroes = result.roomInfo.heroes.map { it.getAvatarData(AvatarSize.RoomListItem) }.toImmutableList(),
                        isTombstoned = result.roomInfo.successorRoom != null
                    ),
                    roomName = result.roomInfo.name ?: result.roomInfo.id.value,
                    content = result.mediaContent,
                    formattedTimestamp = result.formattedTimestamp,
                )
            }
        }
    }
}

@Composable
private fun TextMessageSearchResultItemView(
    avatarData: AvatarData,
    avatarType: AvatarType,
    roomName: String,
    body: String,
    formattedTimestamp: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = avatarType,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = roomName,
                    maxLines = 1,
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
                Text(
                    text = formattedTimestamp,
                    maxLines = 1,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
            Text(
                text = body,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = ElementTheme.typography.fontBodyMdRegular,
                color = ElementTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun MediaMessageSearchResultItemView(
    avatarData: AvatarData,
    avatarType: AvatarType,
    roomName: String,
    content: MediaSearchResultContent,
    formattedTimestamp: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = avatarData,
            avatarType = avatarType,
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = roomName,
                    maxLines = 1,
                    style = ElementTheme.typography.fontBodyLgMedium,
                )
                Text(
                    text = formattedTimestamp,
                    maxLines = 1,
                    style = ElementTheme.typography.fontBodySmRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }

            content.caption?.let {
                Text(
                    text = it,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .background(ElementTheme.colors.bgSubtleSecondary, shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AttachmentThumbnail(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    info = AttachmentThumbnailInfo(
                        type = content.thumbnailType,
                        thumbnailSource = content.thumbnailSource,
                        textContent = null,
                        blurHash = content.blurhash,
                    ),
                    thumbnailSize = 36.dp.roundToPx().toLong(),
                    backgroundColor = ElementTheme.colors.bgCanvasDefault,
                )

                Column {
                    Text(
                        text = content.filename,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = ElementTheme.typography.fontBodyLgRegular,
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        content.extension?.let {
                            Text(
                                text = it,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = ElementTheme.typography.fontBodySmRegular,
                                color = ElementTheme.colors.textSecondary,
                            )
                        }
                        content.formattedSize?.let {
                            Text(
                                text = "($it)",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = ElementTheme.typography.fontBodySmRegular,
                                color = ElementTheme.colors.textSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.addSeparatorLine(color: Color, isLastItem: Boolean): Modifier {
    if (isLastItem) return this

    return this.drawBehind {
        val strokeWidthPx = 1.dp.toPx()
        drawLine(
            color = color,
            start = Offset(0f, size.height - strokeWidthPx),
            end = Offset(size.width, size.height - strokeWidthPx),
            strokeWidth = strokeWidthPx,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun GlobalSearchViewPreview(
    @PreviewParameter(GlobalSearchStatePreviewParam::class) state: GlobalSearchState,
) {
    ElementPreview {
        GlobalSearchView(
            state = state,
            onSelectSearchResult = { _, _ -> },
        )
    }
}
