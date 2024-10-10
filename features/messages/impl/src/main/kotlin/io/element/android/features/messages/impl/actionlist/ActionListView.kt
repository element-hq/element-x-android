/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure.ChangedIdentity
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure.None
import io.element.android.features.messages.impl.crypto.sendfailure.VerifiedUserSendFailure.UnsignedDevice
import io.element.android.features.messages.impl.timeline.components.MessageShieldView
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemCallNotifyContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEncryptedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLegacyCallInviteContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemPollContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemRedactedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStateContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemStickerContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemTextBasedContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemUnknownContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.utils.messagesummary.DefaultMessageSummaryFormatter
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toSp
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.ListItemStyle
import io.element.android.libraries.designsystem.theme.components.ModalBottomSheet
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.hide
import io.element.android.libraries.matrix.ui.messages.sender.SenderName
import io.element.android.libraries.matrix.ui.messages.sender.SenderNameMode
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionListView(
    state: ActionListState,
    onSelectAction: (action: TimelineItemAction, TimelineItem.Event) -> Unit,
    onEmojiReactionClick: (String, TimelineItem.Event) -> Unit,
    onCustomReactionClick: (TimelineItem.Event) -> Unit,
    onVerifiedUserSendFailureClick: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val targetItem = (state.target as? ActionListState.Target.Success)?.event

    fun onItemActionClick(
        itemAction: TimelineItemAction
    ) {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onSelectAction(itemAction, targetItem)
        }
    }

    fun onEmojiReactionClick(emoji: String) {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onEmojiReactionClick(emoji, targetItem)
        }
    }

    fun onCustomReactionClick() {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onCustomReactionClick(targetItem)
        }
    }

    fun onDismiss() {
        state.eventSink(ActionListEvents.Clear)
    }

    fun onVerifiedUserSendFailureClick() {
        if (targetItem == null) return
        sheetState.hide(coroutineScope) {
            state.eventSink(ActionListEvents.Clear)
            onVerifiedUserSendFailureClick(targetItem)
        }
    }

    if (targetItem != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = ::onDismiss,
            modifier = modifier,
        ) {
            SheetContent(
                state = state,
                onActionClick = ::onItemActionClick,
                onEmojiReactionClick = ::onEmojiReactionClick,
                onCustomReactionClick = ::onCustomReactionClick,
                onVerifiedUserSendFailureClick = ::onVerifiedUserSendFailureClick,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    }
}

@Composable
private fun SheetContent(
    state: ActionListState,
    onActionClick: (TimelineItemAction) -> Unit,
    onEmojiReactionClick: (String) -> Unit,
    onCustomReactionClick: () -> Unit,
    onVerifiedUserSendFailureClick: () -> Unit,
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
                            event = target.event,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        )
                        if (target.event.messageShield != null) {
                            MessageShieldView(
                                shield = target.event.messageShield,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        HorizontalDivider()
                    }
                }
                if (target.verifiedUserSendFailure != None) {
                    item {
                        VerifiedUserSendFailureView(
                            sendFailure = target.verifiedUserSendFailure,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onVerifiedUserSendFailureClick
                        )
                        HorizontalDivider()
                    }
                }
                if (target.displayEmojiReactions) {
                    item {
                        EmojiReactionsRow(
                            highlightedEmojis = target.event.reactionsState.highlightedKeys,
                            onEmojiReactionClick = onEmojiReactionClick,
                            onCustomReactionClick = onCustomReactionClick,
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
                            onActionClick(action)
                        },
                        headlineContent = {
                            Text(text = stringResource(id = action.titleRes))
                        },
                        leadingContent = ListItemContent.Icon(IconSource.Resource(action.icon)),
                        style = when {
                            action.destructive -> ListItemStyle.Destructive
                            else -> ListItemStyle.Primary
                        }
                    )
                }
            }
        }
    }
}

@Suppress("MultipleEmitters") // False positive
@Composable
private fun MessageSummary(event: TimelineItem.Event, modifier: Modifier = Modifier) {
    val content: @Composable () -> Unit
    val icon: @Composable () -> Unit = { Avatar(avatarData = event.senderAvatar.copy(size = AvatarSize.MessageActionSender)) }
    val contentStyle = ElementTheme.typography.fontBodyMdRegular.copy(color = MaterialTheme.colorScheme.secondary)

    @Composable
    fun ContentForBody(body: String) {
        Text(body, style = contentStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }

    val context = LocalContext.current
    val formatter = remember(context) { DefaultMessageSummaryFormatter(context) }
    val textContent = remember(event.content) { formatter.format(event) }

    when (event.content) {
        is TimelineItemTextBasedContent,
        is TimelineItemStateContent,
        is TimelineItemEncryptedContent,
        is TimelineItemRedactedContent,
        is TimelineItemUnknownContent -> content = { ContentForBody(textContent) }
        is TimelineItemLocationContent -> {
            content = { ContentForBody(stringResource(CommonStrings.common_shared_location)) }
        }
        is TimelineItemImageContent -> {
            content = { ContentForBody(event.content.bestDescription) }
        }
        is TimelineItemStickerContent -> {
            content = { ContentForBody(event.content.bestDescription) }
        }
        is TimelineItemVideoContent -> {
            content = { ContentForBody(event.content.bestDescription) }
        }
        is TimelineItemFileContent -> {
            content = { ContentForBody(event.content.bestDescription) }
        }
        is TimelineItemAudioContent -> {
            content = { ContentForBody(event.content.bestDescription) }
        }
        is TimelineItemVoiceContent -> {
            content = { ContentForBody(textContent) }
        }
        is TimelineItemPollContent -> {
            content = { ContentForBody(textContent) }
        }
        is TimelineItemLegacyCallInviteContent -> {
            content = { ContentForBody(textContent) }
        }
        is TimelineItemCallNotifyContent -> {
            content = { ContentForBody(stringResource(CommonStrings.common_call_started)) }
        }
    }
    Row(modifier = modifier) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            SenderName(
                senderId = event.senderId,
                senderProfile = event.senderProfile,
                senderNameMode = SenderNameMode.ActionList,
            )
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
    onEmojiReactionClick: (String) -> Unit,
    onCustomReactionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // TODO use most recently used emojis here when available from the Rust SDK
        val defaultEmojis = sequenceOf(
            "👍️",
            "👎️",
            "🔥",
            "❤️",
            "👏"
        )
        for (emoji in defaultEmojis) {
            val isHighlighted = highlightedEmojis.contains(emoji)
            EmojiButton(emoji, isHighlighted, onEmojiReactionClick)
        }
        Box(
            modifier = Modifier
                .size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = CompoundIcons.ReactionAdd(),
                contentDescription = stringResource(id = CommonStrings.a11y_react_with_other_emojis),
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        enabled = true,
                        onClick = onCustomReactionClick,
                        indication = ripple(bounded = false, radius = emojiRippleRadius),
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }
    }
}

@Composable
private fun VerifiedUserSendFailureView(
    sendFailure: VerifiedUserSendFailure,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Composable
    fun VerifiedUserSendFailure.headline(): String {
        return when (this) {
            is None -> ""
            is UnsignedDevice.FromOther -> stringResource(CommonStrings.screen_timeline_item_menu_send_failure_unsigned_device, userDisplayName)
            is UnsignedDevice.FromYou -> stringResource(CommonStrings.screen_timeline_item_menu_send_failure_you_unsigned_device)
            is ChangedIdentity -> stringResource(CommonStrings.screen_timeline_item_menu_send_failure_changed_identity, userDisplayName)
        }
    }

    ListItem(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        leadingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.Error())),
        trailingContent = ListItemContent.Icon(IconSource.Vector(CompoundIcons.ChevronRight())),
        headlineContent = {
            Text(
                text = sendFailure.headline(),
                style = ElementTheme.typography.fontBodySmMedium,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
            leadingIconColor = ElementTheme.colors.iconCriticalPrimary,
            trailingIconColor = ElementTheme.colors.iconPrimary,
            headlineColor = ElementTheme.colors.textCriticalPrimary,
        ),
    )
}

@Composable
private fun EmojiButton(
    emoji: String,
    isHighlighted: Boolean,
    onClick: (String) -> Unit,
) {
    val backgroundColor = if (isHighlighted) {
        ElementTheme.colors.bgActionPrimaryRest
    } else {
        Color.Transparent
    }
    val description = if (isHighlighted) {
        stringResource(id = CommonStrings.a11y_remove_reaction_with, emoji)
    } else {
        stringResource(id = CommonStrings.a11y_react_with, emoji)
    }
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(backgroundColor, CircleShape)
            .clearAndSetSemantics {
                contentDescription = description
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            emoji,
            style = ElementTheme.typography.fontBodyLgRegular.copy(fontSize = 24.dp.toSp(), color = Color.White),
            modifier = Modifier
                .clickable(
                    enabled = true,
                    onClick = { onClick(emoji) },
                    indication = ripple(bounded = false, radius = emojiRippleRadius),
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
        onActionClick = {},
        onEmojiReactionClick = {},
        onCustomReactionClick = {},
        onVerifiedUserSendFailureClick = {},
    )
}
