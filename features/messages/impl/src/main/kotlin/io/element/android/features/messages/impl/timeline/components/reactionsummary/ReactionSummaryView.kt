/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.reactionsummary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.messages.impl.timeline.components.REACTION_IMAGE_ASPECT_RATIO
import io.element.android.features.messages.impl.timeline.model.AggregatedReaction
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toDp
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Surface
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.media.MediaRequestData
import io.element.android.libraries.matrix.ui.model.getAvatarData
import kotlinx.coroutines.launch

internal val REACTION_SUMMARY_LINE_HEIGHT = 25.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactionSummaryView(
    state: ReactionSummaryState,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()

    fun onDismiss() {
        state.eventSink(ReactionSummaryEvents.Clear)
    }

    if (state.target != null) {
        ModalBottomSheet(
            onDismissRequest = ::onDismiss,
            sheetState = sheetState,
            modifier = modifier
        ) {
            ReactionSummaryViewContent(summary = state.target)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReactionSummaryViewContent(
    summary: ReactionSummaryState.Summary,
) {
    val animationScope = rememberCoroutineScope()
    var selectedReactionKey: String by rememberSaveable { mutableStateOf(summary.selectedKey) }
    val selectedReactionIndex: Int by remember {
        derivedStateOf {
            summary.reactions.indexOfFirst { it.key == selectedReactionKey }
        }
    }
    val pagerState = rememberPagerState(initialPage = selectedReactionIndex, pageCount = { summary.reactions.size })
    val reactionListState = rememberLazyListState()

    LaunchedEffect(pagerState.currentPage) {
        selectedReactionKey = summary.reactions[pagerState.currentPage].key
        val visibleInfo = reactionListState.layoutInfo.visibleItemsInfo
        if (selectedReactionIndex <= visibleInfo.first().index || selectedReactionIndex >= visibleInfo.last().index) {
            reactionListState.animateScrollToItem(selectedReactionIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        LazyRow(
            state = reactionListState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp)
        ) {
            items(summary.reactions) { reaction ->
                AggregatedReactionButton(
                    reaction = reaction,
                    isHighlighted = selectedReactionKey == reaction.key,
                    onClick = {
                        selectedReactionKey = reaction.key
                        animationScope.launch {
                            pagerState.animateScrollToPage(selectedReactionIndex)
                        }
                    }
                )
            }
        }
        HorizontalPager(state = pagerState) { page ->
            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(summary.reactions[page].senders) { sender ->

                    val user = sender.user ?: MatrixUser(userId = sender.senderId)

                    SenderRow(
                        avatarData = user.getAvatarData(AvatarSize.UserListItem),
                        name = user.displayName ?: user.userId.value,
                        userId = user.userId.value,
                        sentTime = sender.sentTime
                    )
                }
            }
        }
    }
}

@Composable
private fun AggregatedReactionButton(
    reaction: AggregatedReaction,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    val buttonColor = if (isHighlighted) {
        ElementTheme.colors.bgActionPrimaryRest
    } else {
        Color.Transparent
    }

    val textColor = if (isHighlighted) {
        MaterialTheme.colorScheme.inversePrimary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val roundedCornerShape = RoundedCornerShape(corner = CornerSize(percent = 50))
    Surface(
        modifier = Modifier
            .background(buttonColor, roundedCornerShape)
            .clip(roundedCornerShape)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        color = buttonColor
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier,
        ) {
            // Check if this is a custom reaction (MSC4027)
            if (reaction.key.startsWith("mxc://")) {
                AsyncImage(
                    modifier = Modifier
                        .heightIn(min = REACTION_SUMMARY_LINE_HEIGHT.toDp(), max = REACTION_SUMMARY_LINE_HEIGHT.toDp())
                        .aspectRatio(REACTION_IMAGE_ASPECT_RATIO, false),
                    model = MediaRequestData(MediaSource(reaction.key), MediaRequestData.Kind.Content),
                    contentDescription = null
                )
            } else {
                Text(
                    text = reaction.displayKey,
                    style = ElementTheme.typography.fontBodyMdRegular.copy(
                        fontSize = 20.sp,
                        lineHeight = REACTION_SUMMARY_LINE_HEIGHT
                    ),
                )
            }
            if (reaction.count > 1) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reaction.count.toString(),
                    color = textColor,
                    style = ElementTheme.typography.fontBodyMdRegular.copy(
                        fontSize = 20.sp,
                        lineHeight = REACTION_SUMMARY_LINE_HEIGHT
                    )
                )
            }
        }
    }
}

@Composable
private fun SenderRow(
    avatarData: AvatarData,
    name: String,
    userId: String,
    sentTime: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(avatarData)
        Column(
            modifier = Modifier.padding(start = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .weight(1f),
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.primary,
                    style = ElementTheme.typography.fontBodyMdRegular,
                )
                Text(
                    text = sentTime,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = ElementTheme.typography.fontBodySmRegular,
                )
            }
            Text(
                text = userId,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ElementTheme.typography.fontBodySmRegular,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun ReactionSummaryViewContentPreview(
    @PreviewParameter(ReactionSummaryStateProvider::class) state: ReactionSummaryState
) = ElementPreview {
    ReactionSummaryViewContent(summary = state.target as ReactionSummaryState.Summary)
}
