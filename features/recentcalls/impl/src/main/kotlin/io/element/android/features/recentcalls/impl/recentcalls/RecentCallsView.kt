/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.recentcalls.impl.recentcalls

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.recentcalls.api.RecentCallDirection
import io.element.android.features.recentcalls.api.RecentCallEntry
import io.element.android.features.recentcalls.api.RecentCallStatus
import io.element.android.features.recentcalls.api.RecentCallsFilter
import io.element.android.features.recentcalls.impl.R
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Button
import io.element.android.libraries.designsystem.theme.components.CircularProgressIndicator
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.notification.CallIntent
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun RecentCallsView(
    state: RecentCallsState,
    onRoomClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        RecentCallsFilterRow(
            selectedFilter = state.filter,
            onSelectFilter = { state.eventSink(RecentCallsEvent.SelectFilter(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (state.isLoading && state.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.screen_recent_calls_empty),
                    style = ElementTheme.typography.fontBodyMdRegular,
                    color = ElementTheme.colors.textSecondary,
                )
            }
        } else {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    RecentCallRow(
                        entry = entry,
                        onRoomClick = onRoomClick,
                        onCallBack = { state.eventSink(RecentCallsEvent.CallBack(entry)) },
                    )
                }
                if (state.canLoadMore) {
                    item(key = "load_more") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Button(
                                text = stringResource(CommonStrings.action_load_more),
                                showProgress = state.isLoadingMore,
                                onClick = { state.eventSink(RecentCallsEvent.LoadMore) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentCallsFilterRow(
    selectedFilter: RecentCallsFilter,
    onSelectFilter: (RecentCallsFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        RecentCallsFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onSelectFilter(filter) },
                shape = RoundedCornerShape(
                    topStart = if (index == 0) 12.dp else 0.dp,
                    bottomStart = if (index == 0) 12.dp else 0.dp,
                    topEnd = if (index == RecentCallsFilter.entries.lastIndex) 12.dp else 0.dp,
                    bottomEnd = if (index == RecentCallsFilter.entries.lastIndex) 12.dp else 0.dp,
                ),
            ) {
                Text(
                    text = when (filter) {
                        RecentCallsFilter.ALL -> stringResource(R.string.screen_recent_calls_filter_all)
                        RecentCallsFilter.MISSED -> stringResource(R.string.screen_recent_calls_filter_missed)
                    }
                )
            }
        }
    }
}

@Composable
private fun RecentCallRow(
    entry: RecentCallEntry,
    onRoomClick: (RoomId) -> Unit,
    onCallBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRoomClick(entry.roomId) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            avatarData = AvatarData(
                id = entry.counterpartUserId?.value ?: entry.roomId.value,
                name = entry.roomDisplayName,
                url = entry.avatarUrl,
                size = AvatarSize.RoomListItem,
            ),
            avatarType = if (entry.isDirect) AvatarType.User else AvatarType.Room(),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.roomDisplayName,
                style = ElementTheme.typography.fontBodyLgMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = callSubtitle(entry),
                style = ElementTheme.typography.fontBodySmRegular,
                color = ElementTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(
            imageVector = callDirectionIcon(entry),
            contentDescription = null,
            tint = when (entry.status) {
                RecentCallStatus.MISSED -> ElementTheme.colors.iconCriticalPrimary
                RecentCallStatus.DECLINED -> ElementTheme.colors.iconSecondary
                else -> ElementTheme.colors.iconSecondary
            },
            modifier = Modifier.size(20.dp),
        )
        if (entry.status != RecentCallStatus.ONGOING) {
            IconButton(onClick = onCallBack) {
                Icon(
                    imageVector = if (entry.callIntent == CallIntent.AUDIO) {
                        CompoundIcons.VoiceCall()
                    } else {
                        CompoundIcons.VideoCall()
                    },
                    contentDescription = stringResource(R.string.screen_recent_calls_call_back),
                )
            }
        }
    }
}

@Composable
private fun callSubtitle(entry: RecentCallEntry): String {
    val typeLabel = if (entry.callIntent == CallIntent.AUDIO) {
        stringResource(R.string.screen_recent_calls_voice)
    } else {
        stringResource(R.string.screen_recent_calls_video)
    }
    val statusLabel = when (entry.status) {
        RecentCallStatus.ONGOING -> stringResource(R.string.screen_recent_calls_status_ongoing)
        RecentCallStatus.MISSED -> stringResource(R.string.screen_recent_calls_status_missed)
        RecentCallStatus.DECLINED -> stringResource(R.string.screen_recent_calls_status_declined)
        RecentCallStatus.COMPLETED -> stringResource(R.string.screen_recent_calls_status_completed)
    }
    return "$typeLabel · $statusLabel"
}

@Composable
private fun callDirectionIcon(entry: RecentCallEntry): ImageVector {
    val isAudio = entry.callIntent == CallIntent.AUDIO
    return when (entry.status) {
        RecentCallStatus.MISSED -> if (isAudio) CompoundIcons.VoiceCallMissedSolid() else CompoundIcons.VideoCallMissedSolid()
        RecentCallStatus.DECLINED -> if (isAudio) CompoundIcons.VoiceCallDeclinedSolid() else CompoundIcons.VideoCallDeclinedSolid()
        RecentCallStatus.ONGOING -> if (isAudio) CompoundIcons.VoiceCallSolid() else CompoundIcons.VideoCallSolid()
        RecentCallStatus.COMPLETED -> when (entry.direction) {
            RecentCallDirection.INCOMING -> if (isAudio) CompoundIcons.VoiceCallSolid() else CompoundIcons.VideoCallSolid()
            RecentCallDirection.OUTGOING -> if (isAudio) CompoundIcons.VoiceCallOutgoingSolid() else CompoundIcons.VideoCallOutgoingSolid()
        }
    }
}

@PreviewsDayNight
@Composable
internal fun RecentCallsViewPreview(
    @PreviewParameter(RecentCallsStateProvider::class) state: RecentCallsState,
) = ElementPreview {
    RecentCallsView(
        state = state,
        onRoomClick = {},
    )
}
