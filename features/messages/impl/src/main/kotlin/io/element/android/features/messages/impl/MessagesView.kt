/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.messages.impl.actionlist.ActionListEvents
import io.element.android.features.messages.impl.actionlist.ActionListView
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.messagecomposer.AttachmentsBottomSheet
import io.element.android.features.messages.impl.messagecomposer.AttachmentsState
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerView
import io.element.android.features.messages.impl.messagecomposer.suggestions.SuggestionsPickerView
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerView
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerViewDefaults
import io.element.android.features.messages.impl.timeline.FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineView
import io.element.android.features.messages.impl.timeline.components.JoinCallMenuItem
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionBottomSheet
import io.element.android.features.messages.impl.timeline.components.customreaction.CustomReactionEvents
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryEvents
import io.element.android.features.messages.impl.timeline.components.reactionsummary.ReactionSummaryView
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheet
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetEvents
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageComposerEvents
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessagePermissionRationaleDialog
import io.element.android.features.messages.impl.voicemessages.composer.VoiceMessageSendingFailedDialog
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorView
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.atomic.molecules.IconTitlePlaceholdersRowMolecule
import io.element.android.libraries.designsystem.components.ProgressDialog
import io.element.android.libraries.designsystem.components.ProgressDialogType
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.CompositeAvatar
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.BottomSheetDragHandle
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.IconButton
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.KeepScreenOn
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MessagesView(
    state: MessagesState,
    onBackClick: () -> Unit,
    onRoomDetailsClick: () -> Unit,
    onEventClick: (event: TimelineItem.Event) -> Boolean,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onPreviewAttachments: (ImmutableList<Attachment>) -> Unit,
    onSendLocationClick: () -> Unit,
    onCreatePollClick: () -> Unit,
    onJoinCallClick: () -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
    modifier: Modifier = Modifier,
    forceJumpToBottomVisibility: Boolean = false,
) {
    OnLifecycleEvent { _, event ->
        state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvents.LifecycleEvent(event))
    }

    KeepScreenOn(state.voiceMessageComposerState.keepScreenOn)

    AttachmentStateView(
        state = state.composerState.attachmentsState,
        onPreviewAttachments = onPreviewAttachments,
        onCancel = { state.composerState.eventSink(MessageComposerEvents.CancelSendAttachment) },
    )

    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    // This is needed because the composer is inside an AndroidView that can't be affected by the FocusManager in Compose
    val localView = LocalView.current

    fun onMessageClick(event: TimelineItem.Event) {
        Timber.v("onMessageClick= ${event.id}")
        val hideKeyboard = onEventClick(event)
        if (hideKeyboard) {
            localView.hideKeyboard()
        }
    }

    fun onMessageLongClick(event: TimelineItem.Event) {
        Timber.v("OnMessageLongClicked= ${event.id}")
        localView.hideKeyboard()
        state.actionListState.eventSink(
            ActionListEvents.ComputeForMessage(
                event = event,
                userEventPermissions = state.userEventPermissions,
            )
        )
    }

    fun onActionSelected(action: TimelineItemAction, event: TimelineItem.Event) {
        state.eventSink(MessagesEvents.HandleAction(action, event))
    }

    fun onEmojiReactionClick(emoji: String, event: TimelineItem.Event) {
        state.eventSink(MessagesEvents.ToggleReaction(emoji, event.id))
    }

    fun onEmojiReactionLongClick(emoji: String, event: TimelineItem.Event) {
        if (event.eventId == null) return
        state.reactionSummaryState.eventSink(ReactionSummaryEvents.ShowReactionSummary(event.eventId, event.reactionsState.reactions, emoji))
    }

    fun onMoreReactionsClick(event: TimelineItem.Event) {
        state.customReactionState.eventSink(CustomReactionEvents.ShowCustomReactionSheet(event))
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            Column {
                ConnectivityIndicatorView(isOnline = state.hasNetworkConnection)
                MessagesViewTopBar(
                    roomName = state.roomName.dataOrNull(),
                    roomAvatar = state.roomAvatar.dataOrNull(),
                    heroes = state.heroes,
                    callState = state.callState,
                    onBackClick = {
                        // Since the textfield is now based on an Android view, this is no longer done automatically.
                        // We need to hide the keyboard when navigating out of this screen.
                        localView.hideKeyboard()
                        onBackClick()
                    },
                    onRoomDetailsClick = onRoomDetailsClick,
                    onJoinCallClick = onJoinCallClick,
                )
            }
        },
        content = { padding ->
            MessagesViewContent(
                state = state,
                modifier = Modifier
                    .padding(padding)
                    .consumeWindowInsets(padding),
                onMessageClick = ::onMessageClick,
                onMessageLongClick = ::onMessageLongClick,
                onUserDataClick = onUserDataClick,
                onLinkClick = onLinkClick,
                onReactionClick = ::onEmojiReactionClick,
                onReactionLongClick = ::onEmojiReactionLongClick,
                onMoreReactionsClick = ::onMoreReactionsClick,
                onReadReceiptClick = { event ->
                    state.readReceiptBottomSheetState.eventSink(ReadReceiptBottomSheetEvents.EventSelected(event))
                },
                onSendLocationClick = onSendLocationClick,
                onCreatePollClick = onCreatePollClick,
                onSwipeToReply = { targetEvent ->
                    state.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, targetEvent))
                },
                forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                onJoinCallClick = onJoinCallClick,
                onViewAllPinnedMessagesClick = onViewAllPinnedMessagesClick,
            )
        },
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
    )

    ActionListView(
        state = state.actionListState,
        onSelectAction = ::onActionSelected,
        onCustomReactionClick = { event ->
            state.customReactionState.eventSink(CustomReactionEvents.ShowCustomReactionSheet(event))
        },
        onEmojiReactionClick = ::onEmojiReactionClick,
        onVerifiedUserSendFailureClick = { event ->
            state.timelineState.eventSink(TimelineEvents.ComputeVerifiedUserSendFailure(event))
        },
    )

    CustomReactionBottomSheet(
        state = state.customReactionState,
        onSelectEmoji = { uniqueId, emoji ->
            state.eventSink(MessagesEvents.ToggleReaction(emoji.unicode, uniqueId))
        }
    )

    ReactionSummaryView(state = state.reactionSummaryState)
    ReadReceiptBottomSheet(
        state = state.readReceiptBottomSheetState,
        onUserDataClick = onUserDataClick,
    )
    ReinviteDialog(state = state)
}

@Composable
private fun ReinviteDialog(state: MessagesState) {
    if (state.showReinvitePrompt) {
        ConfirmationDialog(
            title = stringResource(id = R.string.screen_room_invite_again_alert_title),
            content = stringResource(id = R.string.screen_room_invite_again_alert_message),
            cancelText = stringResource(id = CommonStrings.action_cancel),
            submitText = stringResource(id = CommonStrings.action_invite),
            onSubmitClick = { state.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Invite)) },
            onDismiss = { state.eventSink(MessagesEvents.InviteDialogDismissed(InviteDialogAction.Cancel)) }
        )
    }
}

@Composable
private fun AttachmentStateView(
    state: AttachmentsState,
    onPreviewAttachments: (ImmutableList<Attachment>) -> Unit,
    onCancel: () -> Unit,
) {
    when (state) {
        AttachmentsState.None -> Unit
        is AttachmentsState.Previewing -> {
            val latestOnPreviewAttachments by rememberUpdatedState(onPreviewAttachments)
            LaunchedEffect(state) {
                latestOnPreviewAttachments(state.attachments)
            }
        }
        is AttachmentsState.Sending -> {
            ProgressDialog(
                type = when (state) {
                    is AttachmentsState.Sending.Uploading -> ProgressDialogType.Determinate(state.progress)
                    is AttachmentsState.Sending.Processing -> ProgressDialogType.Indeterminate
                },
                text = stringResource(id = CommonStrings.common_sending),
                showCancelButton = true,
                onDismissRequest = onCancel,
            )
        }
    }
}

@Composable
private fun MessagesViewContent(
    state: MessagesState,
    onMessageClick: (TimelineItem.Event) -> Unit,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onReactionClick: (key: String, TimelineItem.Event) -> Unit,
    onReactionLongClick: (key: String, TimelineItem.Event) -> Unit,
    onMoreReactionsClick: (TimelineItem.Event) -> Unit,
    onReadReceiptClick: (TimelineItem.Event) -> Unit,
    onMessageLongClick: (TimelineItem.Event) -> Unit,
    onSendLocationClick: () -> Unit,
    onCreatePollClick: () -> Unit,
    onJoinCallClick: () -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
    forceJumpToBottomVisibility: Boolean,
    modifier: Modifier = Modifier,
    onSwipeToReply: (TimelineItem.Event) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        AttachmentsBottomSheet(
            state = state.composerState,
            onSendLocationClick = onSendLocationClick,
            onCreatePollClick = onCreatePollClick,
            enableTextFormatting = state.enableTextFormatting,
        )

        if (state.enableVoiceMessages && state.voiceMessageComposerState.showPermissionRationaleDialog) {
            VoiceMessagePermissionRationaleDialog(
                onContinue = {
                    state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvents.AcceptPermissionRationale)
                },
                onDismiss = {
                    state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvents.DismissPermissionsRationale)
                },
                appName = state.appName
            )
        }
        if (state.enableVoiceMessages && state.voiceMessageComposerState.showSendFailureDialog) {
            VoiceMessageSendingFailedDialog(
                onDismiss = { state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvents.DismissSendFailureDialog) },
            )
        }

        // This key is used to force the sheet to be remeasured when the content changes.
        // Any state change that should trigger a height size should be added to the list of remembered values here.
        val sheetResizeContentKey = remember { mutableIntStateOf(0) }
        LaunchedEffect(
            state.composerState.textEditorState.lineCount,
            state.composerState.showTextFormatting,
        ) {
            sheetResizeContentKey.intValue = Random.nextInt()
        }

        ExpandableBottomSheetScaffold(
            sheetDragHandle = if (state.composerState.showTextFormatting) {
                @Composable { BottomSheetDragHandle() }
            } else {
                @Composable {}
            },
            sheetSwipeEnabled = state.composerState.showTextFormatting,
            sheetShape = if (state.composerState.showTextFormatting || state.composerState.suggestions.isNotEmpty()) {
                MaterialTheme.shapes.large
            } else {
                RectangleShape
            },
            content = { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    val scrollBehavior = PinnedMessagesBannerViewDefaults.rememberExitOnScrollBehavior()
                    TimelineView(
                        state = state.timelineState,
                        onUserDataClick = onUserDataClick,
                        onLinkClick = onLinkClick,
                        onMessageClick = onMessageClick,
                        onMessageLongClick = onMessageLongClick,
                        onSwipeToReply = onSwipeToReply,
                        onReactionClick = onReactionClick,
                        onReactionLongClick = onReactionLongClick,
                        onMoreReactionsClick = onMoreReactionsClick,
                        onReadReceiptClick = onReadReceiptClick,
                        forceJumpToBottomVisibility = forceJumpToBottomVisibility,
                        onJoinCallClick = onJoinCallClick,
                        nestedScrollConnection = scrollBehavior.nestedScrollConnection,
                    )
                    AnimatedVisibility(
                        visible = state.pinnedMessagesBannerState is PinnedMessagesBannerState.Visible && scrollBehavior.isVisible,
                        enter = expandVertically(),
                        exit = shrinkVertically(),
                    ) {
                        fun focusOnPinnedEvent(eventId: EventId) {
                            state.timelineState.eventSink(
                                TimelineEvents.FocusOnEvent(eventId = eventId, debounce = FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS.milliseconds)
                            )
                        }
                        PinnedMessagesBannerView(
                            state = state.pinnedMessagesBannerState,
                            onClick = ::focusOnPinnedEvent,
                            onViewAllClick = onViewAllPinnedMessagesClick,
                        )
                    }
                }
            },
            sheetContent = { subcomposing: Boolean ->
                MessagesViewComposerBottomSheetContents(
                    subcomposing = subcomposing,
                    state = state,
                )
            },
            sheetContentKey = sheetResizeContentKey.intValue,
            sheetTonalElevation = 0.dp,
            sheetShadowElevation = if (state.composerState.suggestions.isNotEmpty()) 16.dp else 0.dp,
        )
    }
}

@Composable
private fun MessagesViewComposerBottomSheetContents(
    subcomposing: Boolean,
    state: MessagesState,
) {
    if (state.userEventPermissions.canSendMessage) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SuggestionsPickerView(
                modifier = Modifier
                    .heightIn(max = 230.dp)
                    // Consume all scrolling, preventing the bottom sheet from being dragged when interacting with the list of suggestions
                    .nestedScroll(object : NestedScrollConnection {
                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            return available
                        }
                    }),
                roomId = state.roomId,
                roomName = state.roomName.dataOrNull(),
                roomAvatarData = state.roomAvatar.dataOrNull(),
                suggestions = state.composerState.suggestions,
                onSelectSuggestion = {
                    state.composerState.eventSink(MessageComposerEvents.InsertSuggestion(it))
                }
            )
            MessageComposerView(
                state = state.composerState,
                voiceMessageState = state.voiceMessageComposerState,
                subcomposing = subcomposing,
                enableVoiceMessages = state.enableVoiceMessages,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        CantSendMessageBanner()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesViewTopBar(
    roomName: String?,
    roomAvatar: AvatarData?,
    heroes: ImmutableList<AvatarData>,
    callState: RoomCallState,
    onRoomDetailsClick: () -> Unit,
    onJoinCallClick: () -> Unit,
    onBackClick: () -> Unit,
) {
    TopAppBar(
        navigationIcon = {
            BackButton(onClick = onBackClick)
        },
        title = {
            val roundedCornerShape = RoundedCornerShape(8.dp)
            val titleModifier = Modifier
                .clip(roundedCornerShape)
                .clickable { onRoomDetailsClick() }
            if (roomName != null && roomAvatar != null) {
                RoomAvatarAndNameRow(
                    roomName = roomName,
                    roomAvatar = roomAvatar,
                    heroes = heroes,
                    modifier = titleModifier
                )
            } else {
                IconTitlePlaceholdersRowMolecule(
                    iconSize = AvatarSize.TimelineRoom.dp,
                    modifier = titleModifier
                )
            }
        },
        actions = {
            CallMenuItem(
                isCallOngoing = callState == RoomCallState.ONGOING,
                onClick = onJoinCallClick,
                enabled = callState != RoomCallState.DISABLED
            )
            Spacer(Modifier.width(8.dp))
        },
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun CallMenuItem(
    isCallOngoing: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    if (isCallOngoing) {
        JoinCallMenuItem(onJoinCallClick = onClick)
    } else {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = CompoundIcons.VideoCallSolid(),
                contentDescription = stringResource(CommonStrings.a11y_start_call),
            )
        }
    }
}

@Composable
private fun RoomAvatarAndNameRow(
    roomName: String,
    roomAvatar: AvatarData,
    heroes: ImmutableList<AvatarData>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositeAvatar(
            avatarData = roomAvatar,
            heroes = heroes,
        )
        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = roomName,
            style = ElementTheme.typography.fontBodyLgMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CantSendMessageBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.screen_room_timeline_no_permission_to_post),
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
        )
    }
}

@PreviewsDayNight
@Composable
internal fun MessagesViewPreview(@PreviewParameter(MessagesStateProvider::class) state: MessagesState) = ElementPreview {
    MessagesView(
        state = state,
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventClick = { false },
        onUserDataClick = {},
        onLinkClick = {},
        onPreviewAttachments = {},
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = { },
        forceJumpToBottomVisibility = true,
    )
}
