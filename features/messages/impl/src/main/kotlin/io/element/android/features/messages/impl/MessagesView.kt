/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
import io.element.android.features.messages.impl.crypto.identity.IdentityChangeStateView
import io.element.android.features.messages.impl.link.LinkEvents
import io.element.android.features.messages.impl.link.LinkView
import io.element.android.features.messages.impl.messagecomposer.AttachmentsBottomSheet
import io.element.android.features.messages.impl.messagecomposer.DisabledComposerView
import io.element.android.features.messages.impl.messagecomposer.MessageComposerEvents
import io.element.android.features.messages.impl.messagecomposer.MessageComposerView
import io.element.android.features.messages.impl.messagecomposer.suggestions.SuggestionsPickerView
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerState
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerView
import io.element.android.features.messages.impl.pinned.banner.PinnedMessagesBannerViewDefaults
import io.element.android.features.messages.impl.timeline.FOCUS_ON_PINNED_EVENT_DEBOUNCE_DURATION_IN_MILLIS
import io.element.android.features.messages.impl.timeline.TimelineEvents
import io.element.android.features.messages.impl.timeline.TimelineView
import io.element.android.features.messages.impl.timeline.components.CallMenuItem
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
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.atomic.molecules.ComposerAlertMolecule
import io.element.android.libraries.designsystem.components.ExpandableBottomSheetLayout
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarType
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.dialogs.ConfirmationDialog
import io.element.android.libraries.designsystem.components.rememberExpandableBottomSheetLayoutState
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.text.toAnnotatedString
import io.element.android.libraries.designsystem.theme.components.BottomSheetDragHandle
import io.element.android.libraries.designsystem.theme.components.Icon
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.Text
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.designsystem.utils.HideKeyboardWhenDisposed
import io.element.android.libraries.designsystem.utils.KeepScreenOn
import io.element.android.libraries.designsystem.utils.OnLifecycleEvent
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.wysiwyg.link.Link
import kotlinx.collections.immutable.ImmutableList
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun MessagesView(
    state: MessagesState,
    onBackClick: () -> Unit,
    onRoomDetailsClick: () -> Unit,
    onEventContentClick: (isLive: Boolean, event: TimelineItem.Event) -> Boolean,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String, Boolean) -> Unit,
    onSendLocationClick: () -> Unit,
    onCreatePollClick: () -> Unit,
    onJoinCallClick: () -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
    modifier: Modifier = Modifier,
    forceJumpToBottomVisibility: Boolean = false,
    knockRequestsBannerView: @Composable () -> Unit,
) {
    OnLifecycleEvent { _, event ->
        state.voiceMessageComposerState.eventSink(VoiceMessageComposerEvents.LifecycleEvent(event))
    }

    KeepScreenOn(state.voiceMessageComposerState.keepScreenOn)

    HideKeyboardWhenDisposed()

    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    // This is needed because the composer is inside an AndroidView that can't be affected by the FocusManager in Compose
    val localView = LocalView.current

    fun hidingKeyboard(block: () -> Unit) {
        localView.hideKeyboard()
        block()
    }

    fun onContentClick(event: TimelineItem.Event) {
        Timber.v("onMessageClick= ${event.id}")
        val hideKeyboard = onEventContentClick(state.timelineState.isLive, event)
        if (hideKeyboard) {
            localView.hideKeyboard()
        }
    }

    fun onMessageLongClick(event: TimelineItem.Event) {
        Timber.v("OnMessageLongClicked= ${event.id}")
        hidingKeyboard {
            state.actionListState.eventSink(
                ActionListEvents.ComputeForMessage(
                    event = event,
                    userEventPermissions = state.userEventPermissions,
                )
            )
        }
    }

    fun onActionSelected(action: TimelineItemAction, event: TimelineItem.Event) {
        state.eventSink(MessagesEvents.HandleAction(action, event))
    }

    fun onEmojiReactionClick(emoji: String, event: TimelineItem.Event) {
        state.eventSink(MessagesEvents.ToggleReaction(emoji, event.eventOrTransactionId))
    }

    fun onEmojiReactionLongClick(emoji: String, event: TimelineItem.Event) {
        if (event.eventId == null) return
        state.reactionSummaryState.eventSink(ReactionSummaryEvents.ShowReactionSummary(event.eventId, event.reactionsState.reactions, emoji))
    }

    fun onMoreReactionsClick(event: TimelineItem.Event) {
        state.customReactionState.eventSink(CustomReactionEvents.ShowCustomReactionSheet(event))
    }

    val expandableState = rememberExpandableBottomSheetLayoutState()
    ExpandableBottomSheetLayout(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .systemBarsPadding(),
        content = {
            Scaffold(
                contentWindowInsets = WindowInsets.statusBars,
                topBar = {
                    Column {
                        ConnectivityIndicatorView(isOnline = state.hasNetworkConnection)
                        MessagesViewTopBar(
                            roomName = state.roomName,
                            roomAvatar = state.roomAvatar,
                            isTombstoned = state.isTombstoned,
                            heroes = state.heroes,
                            roomCallState = state.roomCallState,
                            dmUserIdentityState = state.dmUserVerificationState,
                            onBackClick = { hidingKeyboard { onBackClick() } },
                            onRoomDetailsClick = { hidingKeyboard { onRoomDetailsClick() } },
                            onJoinCallClick = onJoinCallClick,
                        )
                    }
                },
                content = { padding ->
                    Box(
                        modifier = Modifier
                            .padding(padding)
                            .consumeWindowInsets(padding)
                    ) {
                        MessagesViewContent(
                            state = state,
                            onContentClick = ::onContentClick,
                            onMessageLongClick = ::onMessageLongClick,
                            onUserDataClick = {
                                hidingKeyboard {
                                    state.eventSink(MessagesEvents.OnUserClicked(it))
                                }
                            },
                            onLinkClick = { link, customTab ->
                                if (customTab) {
                                    onLinkClick(link.url, true)
                                    // Do not check those links, they are internal link only
                                } else {
                                    state.linkState.eventSink(LinkEvents.OnLinkClick(link))
                                }
                            },
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
                            knockRequestsBannerView = knockRequestsBannerView,
                        )

                        SuggestionsPickerView(
                            modifier = Modifier
                                .shadow(10.dp)
                                .background(ElementTheme.colors.bgCanvasDefault)
                                .align(Alignment.BottomStart)
                                .heightIn(max = 230.dp),
                            roomId = state.roomId,
                            roomName = state.roomName,
                            roomAvatarData = state.roomAvatar,
                            suggestions = state.composerState.suggestions,
                            onSelectSuggestion = {
                                state.composerState.eventSink(MessageComposerEvents.InsertSuggestion(it))
                            }
                        )
                    }
                },
                snackbarHost = {
                    SnackbarHost(
                        snackbarHostState,
                        modifier = Modifier.navigationBarsPadding()
                    )
                },
            )
        },
        bottomSheetContent = {
            MessagesViewComposerBottomSheetContents(
                state = state,
                onLinkClick = { url, customTab -> onLinkClick(url, customTab) },
                onRoomSuccessorClick = { roomId ->
                    state.timelineState.eventSink(TimelineEvents.NavigateToRoom(roomId = roomId))
                },
            )
        },
        sheetDragHandle = if (state.composerState.showTextFormatting) {
            @Composable { BottomSheetDragHandle() }
        } else {
            @Composable {}
        },
        isSwipeGestureEnabled = state.composerState.showTextFormatting,
        state = expandableState,
        sheetShape = if (state.composerState.showTextFormatting || state.composerState.suggestions.isNotEmpty()) {
            MaterialTheme.shapes.large
        } else {
            RectangleShape
        },
        maxBottomSheetContentHeight = 360.dp,
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
    LinkView(
        onLinkValid = { link ->
            onLinkClick(link.url, false)
        },
        state = state.linkState,
    )
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
private fun MessagesViewContent(
    state: MessagesState,
    onContentClick: (TimelineItem.Event) -> Unit,
    onUserDataClick: (MatrixUser) -> Unit,
    onLinkClick: (Link, Boolean) -> Unit,
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
    onSwipeToReply: (TimelineItem.Event) -> Unit,
    modifier: Modifier = Modifier,
    knockRequestsBannerView: @Composable () -> Unit,
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

        Box {
            val scrollBehavior = PinnedMessagesBannerViewDefaults.rememberScrollBehavior(
                pinnedMessagesCount = (state.pinnedMessagesBannerState as? PinnedMessagesBannerState.Visible)?.pinnedMessagesCount() ?: 0,
            )
            TimelineView(
                state = state.timelineState,
                timelineProtectionState = state.timelineProtectionState,
                onUserDataClick = onUserDataClick,
                onLinkClick = { link -> onLinkClick(link, false) },
                onContentClick = onContentClick,
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
            knockRequestsBannerView()
        }
    }
}

@Composable
private fun MessagesViewComposerBottomSheetContents(
    state: MessagesState,
    onRoomSuccessorClick: (RoomId) -> Unit,
    onLinkClick: (String, Boolean) -> Unit,
) {
    when {
        state.successorRoom != null -> {
            SuccessorRoomBanner(roomSuccessor = state.successorRoom, onRoomSuccessorClick = onRoomSuccessorClick)
        }
        state.userEventPermissions.canSendMessage -> {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Do not show the identity change if user is composing a Rich message or is seeing suggestion(s).
                if (state.composerState.suggestions.isEmpty() &&
                    state.composerState.textEditorState is TextEditorState.Markdown) {
                    IdentityChangeStateView(
                        state = state.identityChangeState,
                        onLinkClick = onLinkClick,
                    )
                }
                val verificationViolation = state.identityChangeState.roomMemberIdentityStateChanges.firstOrNull {
                    it.identityState == IdentityState.VerificationViolation
                }
                if (verificationViolation != null) {
                    DisabledComposerView(modifier = Modifier.fillMaxWidth())
                } else {
                    MessageComposerView(
                        state = state.composerState,
                        voiceMessageState = state.voiceMessageComposerState,
                        enableVoiceMessages = state.enableVoiceMessages,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
        else -> {
            CantSendMessageBanner()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MessagesViewTopBar(
    roomName: String?,
    roomAvatar: AvatarData,
    isTombstoned: Boolean,
    heroes: ImmutableList<AvatarData>,
    roomCallState: RoomCallState,
    dmUserIdentityState: IdentityState?,
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
            Row(
                modifier = Modifier
                    .clip(roundedCornerShape)
                    .clickable { onRoomDetailsClick() },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val titleModifier = Modifier.weight(1f, fill = false)
                RoomAvatarAndNameRow(
                    roomName = roomName,
                    roomAvatar = roomAvatar,
                    isTombstoned = isTombstoned,
                    heroes = heroes,
                    modifier = titleModifier
                )

                when (dmUserIdentityState) {
                    IdentityState.Verified -> {
                        Icon(
                            imageVector = CompoundIcons.Verified(),
                            tint = ElementTheme.colors.iconSuccessPrimary,
                            contentDescription = null,
                        )
                    }
                    IdentityState.VerificationViolation -> {
                        Icon(
                            imageVector = CompoundIcons.ErrorSolid(),
                            tint = ElementTheme.colors.iconCriticalPrimary,
                            contentDescription = null,
                        )
                    }
                    else -> Unit
                }
            }
        },
        actions = {
            CallMenuItem(
                roomCallState = roomCallState,
                onJoinCallClick = onJoinCallClick,
            )
            Spacer(Modifier.width(8.dp))
        },
        windowInsets = WindowInsets(0.dp)
    )
}

@Composable
private fun RoomAvatarAndNameRow(
    roomName: String?,
    roomAvatar: AvatarData,
    heroes: ImmutableList<AvatarData>,
    isTombstoned: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            avatarData = roomAvatar,
            avatarType = AvatarType.Room(
                heroes = heroes,
                isTombstoned = isTombstoned,
            ),
        )
        Text(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .semantics {
                    heading()
                },
            text = roomName ?: stringResource(CommonStrings.common_no_room_name),
            style = ElementTheme.typography.fontBodyLgMedium,
            fontStyle = FontStyle.Italic.takeIf { roomName == null },
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
            .background(ElementTheme.colors.bgSubtleSecondary)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.screen_room_timeline_no_permission_to_post),
            color = ElementTheme.colors.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
        )
    }
}

@Composable
private fun SuccessorRoomBanner(
    roomSuccessor: SuccessorRoom,
    onRoomSuccessorClick: (RoomId) -> Unit,
    modifier: Modifier = Modifier,
) {
    ComposerAlertMolecule(
        avatar = null,
        content = stringResource(R.string.screen_room_timeline_tombstoned_room_message).toAnnotatedString(),
        onSubmitClick = { onRoomSuccessorClick(roomSuccessor.roomId) },
        modifier = modifier,
        isCritical = false,
        submitText = stringResource(R.string.screen_room_timeline_tombstoned_room_action)
    )
}

@PreviewsDayNight
@Composable
internal fun MessagesViewPreview(@PreviewParameter(MessagesStateProvider::class) state: MessagesState) = ElementPreview {
    MessagesView(
        state = state,
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventContentClick = { _, _ -> false },
        onUserDataClick = {},
        onLinkClick = { _, _ -> },
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = { },
        forceJumpToBottomVisibility = true,
        knockRequestsBannerView = {},
    )
}
