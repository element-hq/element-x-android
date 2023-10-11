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

package io.element.android.features.messages.impl.actionlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.utils.messagesummary.MessageSummaryFormatterImpl
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.designsystem.utils.CommonDrawables
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnail
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailInfo
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.theme.ElementTheme
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionListView(
    state: ActionListState,
    onActionSelected: (action: TimelineItemAction, TimelineItem.Event) -> Unit,
    onEmojiReactionClicked: (String, TimelineItem.Event) -> Unit,
    onCustomReactionClicked: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val targetItem = (state.target as? ActionListState.Target.Success)?.event

    fun onItemActionClicked(
        itemAction: TimelineItemAction
    ) {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onActionSelected(itemAction, targetItem)
        }
    }

    fun onEmojiReactionClicked(emoji: String) {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onEmojiReactionClicked(emoji, targetItem)
        }
    }

    fun onCustomReactionClicked() {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onCustomReactionClicked(targetItem)
        }
    }

    fun onDismiss() {
        state.eventSink(ActionListEvents.Clear)
    }

    if (targetItem != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = ::onDismiss,
            modifier = modifier,
        ) {
            SheetContent(
                state = state,
                onActionClicked = ::onItemActionClicked,
                onEmojiReactionClicked = ::onEmojiReactionClicked,
                onCustomReactionClicked = ::onCustomReactionClicked,
                modifier = Modifier
                    .padding(bottom = 32.dp)
//                    .navigationBarsPadding() - FIXME after https://issuetracker.google.com/issues/275849044
//                    .imePadding()
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SheetContent(
    state: ActionListState,
    onActionClicked: (TimelineItemAction) -> Unit,
    onEmojiReactionClicked: (String) -> Unit,
    onCustomReactionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val target = state.target) {
        is ActionListState.Target.Loading,
        ActionListState.Target.None -> {
            // Crashes if sheetContent size is zero
            Box(modifier = modifier.size(1.dp))
        }

        is ActionListState.Target.Success -> {
            val actions = target.actions
            LazyColumn(
                modifier = modifier.fillMaxWidth()
            ) {
                item {
                    Column {
                        MessageSummary(
                            event = target.event, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider()
                    }
                }
                if (state.displayEmojiReactions) {
                    item {
                        EmojiReactionsRow(
                            highlightedEmojis = target.event.reactionsState.highlightedKeys,
                            onEmojiReactionClicked = onEmojiReactionClicked,
                            onCustomReactionClicked = onCustomReactionClicked,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                    }
                }
                items(
                    items = actions,
                ) { action ->
                    ListItem(
                        modifier = Modifier.clickable {
                            onActionClicked(action)
                        },
                        text = {
                            Text(
                                text = stringResource(id = action.titleRes),
                                color = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        },
                        icon = {
                            Icon(
                                resourceId = action.icon,
                                contentDescription = "",
                                tint = if (action.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageSummary(event: TimelineItem.Event, modifier: Modifier = Modifier) {
    val content: @Composable () -> Unit
    var icon: @Composable () -> Unit = { Avatar(avatarData = event.senderAvatar.copy(size = AvatarSize.MessageActionSender)) }
    val contentStyle = ElementTheme.typography.fontBodyMdRegular.copy(color = MaterialTheme.colorScheme.secondary)
    val imageModifier = Modifier
        .size(AvatarSize.MessageActionSender.dp)
        .clip(RoundedCornerShape(9.dp))

    @Composable
    fun ContentForBody(body: String) {
        Text(body, style = contentStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }

    val context = LocalContext.current
    val formatter = remember(context) { MessageSummaryFormatterImpl(context) }
    val textContent = remember(event.content) { formatter.format(event) }

    when (event.content) {
        is TimelineItemPollContent, // TODO Polls: handle summary
        is TimelineItemTextBasedContent,
        is TimelineItemStateContent,
        is TimelineItemEncryptedContent,
        is TimelineItemRedactedContent,
        is TimelineItemUnknownContent -> content = { ContentForBody(textContent) }
        is TimelineItemLocationContent -> {
            icon = {
                AttachmentThumbnail(
                    modifier = imageModifier,
                    info = AttachmentThumbnailInfo(
                        type = AttachmentThumbnailType.Location,
                        textContent = stringResource(CommonStrings.common_shared_location),
                    )
                )
            }
            content = { ContentForBody(stringResource(CommonStrings.common_shared_location)) }
        }
        is TimelineItemImageContent -> {
            icon = {
                AttachmentThumbnail(
                    modifier = imageModifier,
                    info = AttachmentThumbnailInfo(
                        thumbnailSource = event.content.mediaSource,
                        textContent = textContent,
                        type = AttachmentThumbnailType.Image,
                        blurHash = event.content.blurhash,
                    )
                )
            }
            content = { ContentForBody(event.content.body) }
        }
        is TimelineItemVideoContent -> {
            icon = {
                AttachmentThumbnail(
                    modifier = imageModifier,
                    info = AttachmentThumbnailInfo(
                        thumbnailSource = event.content.thumbnailSource,
                        textContent = textContent,
                        type = AttachmentThumbnailType.Video,
                        blurHash = event.content.blurHash,
                    )
                )
            }
            content = { ContentForBody(event.content.body) }
        }
        is TimelineItemFileContent -> {
            icon = {
                AttachmentThumbnail(
                    modifier = imageModifier,
                    info = AttachmentThumbnailInfo(
                        thumbnailSource = event.content.thumbnailSource,
                        textContent = textContent,
                        type = AttachmentThumbnailType.File,
                    )
                )
            }
            content = { ContentForBody(event.content.body) }
        }
        is TimelineItemAudioContent -> {
            icon = {
                AttachmentThumbnail(
                    modifier = imageModifier,
                    info = AttachmentThumbnailInfo(
                        textContent = textContent,
                        type = AttachmentThumbnailType.Audio,
                    )
                )
            }
            content = { ContentForBody(event.content.body) }
        }
    }
    Row(modifier = modifier) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row {
                if (event.senderDisplayName != null) {
                    Text(
                        text = event.senderDisplayName,
                        style = ElementTheme.typography.fontBodySmMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            content()
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            event.sentTime,
            style = ElementTheme.typography.fontBodyXsRegular,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.End,
        )
    }
}

private val emojiRippleRadius = 24.dp

@Composable
private fun EmojiReactionsRow(
    highlightedEmojis: ImmutableList<String>,
    onEmojiReactionClicked: (String) -> Unit,
    onCustomReactionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // TODO use most recently used emojis here when available from the Rust SDK
        val defaultEmojis = sequenceOf(
            "👍", "👎", "🔥", "❤️", "👏"
        )
        for (emoji in defaultEmojis) {
            val isHighlighted = highlightedEmojis.contains(emoji)
            EmojiButton(emoji, isHighlighted, onEmojiReactionClicked)
        }
        Box(
            modifier = Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                resourceId = CommonDrawables.ic_september_add_reaction,
                contentDescription = "Emojis",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        enabled = true,
                        onClick = onCustomReactionClicked,
                        indication = rememberRipple(bounded = false, radius = emojiRippleRadius),
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }
    }
}

@Composable
private fun EmojiButton(
    emoji: String,
    isHighlighted: Boolean,
    onClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = if (isHighlighted) {
        ElementTheme.colors.bgActionPrimaryRest
    } else {
        Color.Transparent
    }
    Box(
        modifier = modifier
            .size(48.dp)
            .background(backgroundColor, CircleShape),

        contentAlignment = Alignment.Center
    ) {
        Text(
            emoji,
            fontSize = 24.dp.toSp(),
            color = Color.White,
            modifier = Modifier
                .clickable(
                    enabled = true,
                    onClick = { onClicked(emoji) },
                    indication = rememberRipple(bounded = false, radius = emojiRippleRadius),
                    interactionSource = remember { MutableInteractionSource() }
                )
        )
    }
}

@PreviewsDayNight
@Composable
internal fun SheetContentPreview(
    @PreviewParameter(ActionListStateProvider::class) state: ActionListState
) = ElementPreview {
    SheetContent(
        state = state,
        onActionClicked = {},
        onEmojiReactionClicked = {},
        onCustomReactionClicked = {},
    )
}
