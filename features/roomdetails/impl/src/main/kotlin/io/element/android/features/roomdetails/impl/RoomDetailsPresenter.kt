/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dev.zacsweers.metro.Inject
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.knockrequests.api.KnockRequestPermissions
import io.element.android.features.knockrequests.api.knockRequestPermissions
import io.element.android.features.leaveroom.api.LeaveRoomEvent
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomcall.api.RoomCallState
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsPresenter
import io.element.android.features.roomdetailsedit.api.RoomDetailsEditPermissions
import io.element.android.features.roomdetailsedit.api.roomDetailsEditPermissions
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyPermissions
import io.element.android.features.securityandprivacy.api.securityAndPrivacyPermissions
import io.element.android.libraries.androidutils.clipboard.ClipboardHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.designsystem.utils.snackbar.LocalSnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.designsystem.utils.snackbar.collectSnackbarMessageAsState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.identity.IdentityState
import io.element.android.libraries.matrix.api.notificationsettings.NotificationSettingsService
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.powerlevels.canEditRolesAndPermissions
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState
import io.element.android.libraries.matrix.api.room.roomNotificationSettings
import io.element.android.libraries.matrix.ui.room.getCurrentRoomMember
import io.element.android.libraries.matrix.ui.room.getDirectRoomMember
import io.element.android.libraries.matrix.ui.room.roomMemberIdentityStateChange
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Inject
class RoomDetailsPresenter(
    private val client: MatrixClient,
    private val room: JoinedRoom,
    private val featureFlagService: FeatureFlagService,
    private val notificationSettingsService: NotificationSettingsService,
    private val roomMembersDetailsPresenterFactory: RoomMemberDetailsPresenter.Factory,
    private val leaveRoomPresenter: Presenter<LeaveRoomState>,
    private val roomCallStatePresenter: Presenter<RoomCallState>,
    private val dispatchers: CoroutineDispatchers,
    private val analyticsService: AnalyticsService,
    private val clipboardHelper: ClipboardHelper,
    private val appPreferencesStore: AppPreferencesStore,
) : Presenter<RoomDetailsState> {
    @Composable
    override fun present(): RoomDetailsState {
        val scope = rememberCoroutineScope()
        val leaveRoomState = leaveRoomPresenter.present()
        val roomInfo by room.roomInfoFlow.collectAsState()
        val roomAvatar by remember { derivedStateOf { roomInfo.avatarUrl } }

        val roomName by remember { derivedStateOf { roomInfo.name?.trim().orEmpty() } }
        val roomTopic by remember { derivedStateOf { roomInfo.topic } }
        val isFavorite by remember { derivedStateOf { roomInfo.isFavorite } }
        val joinRule by remember { derivedStateOf { roomInfo.joinRule } }

        val pinnedMessagesCount by remember { derivedStateOf { roomInfo.pinnedEventIds.size } }

        LaunchedEffect(Unit) {
            room.updateRoomNotificationSettings()
            observeNotificationSettings()
        }

        val isDm = roomInfo.isDm
        val membersState by room.membersStateFlow.collectAsState()
        val permissions by getPermissions()
        val canonicalAlias by remember { derivedStateOf { roomInfo.canonicalAlias } }
        val isEncrypted by remember { derivedStateOf { roomInfo.isEncrypted == true } }
        val dmMember by room.getDirectRoomMember(membersState)
        val currentMember by room.getCurrentRoomMember(membersState)
        val roomMemberDetailsPresenter = roomMemberDetailsPresenter(dmMember)
        val roomType = getRoomType(dmMember, currentMember)
        val roomCallState = roomCallStatePresenter.present()
        val joinedMemberCount by remember { derivedStateOf { roomInfo.joinedMembersCount } }

        val topicState = remember(permissions.editDetailsPermissions.canEditTopic, roomTopic, roomType) {
            val topic = roomTopic
            when {
                !topic.isNullOrBlank() -> RoomTopicState.ExistingTopic(topic)
                permissions.editDetailsPermissions.canEditTopic && roomType is RoomDetailsType.Room -> RoomTopicState.CanAddTopic
                else -> RoomTopicState.Hidden
            }
        }

        val isKnockRequestsEnabled by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.Knock)
        }.collectAsState(false)
        val knockRequestsCount by produceState<Int?>(null) {
            room.knockRequestsFlow.collect { value = it.size }
        }
        val canShowKnockRequests by remember {
            derivedStateOf { isKnockRequestsEnabled && permissions.knockRequestsPermissions.hasAny && joinRule == JoinRule.Knock }
        }
        val canShowSecurityAndPrivacy by remember {
            derivedStateOf { !isDm && permissions.securityAndPrivacyPermissions.hasAny(isSpace = false, joinRule = joinRule) }
        }
        val isDeveloperModeEnabled by remember {
            appPreferencesStore.isDeveloperModeEnabledFlow()
        }.collectAsState(initial = false)

        val roomNotificationSettingsState by room.roomNotificationSettingsStateFlow.collectAsState()

        val snackbarDispatcher = LocalSnackbarDispatcher.current
        val snackbarMessage by snackbarDispatcher.collectSnackbarMessageAsState()

        fun handleEvent(event: RoomDetailsEvent) {
            when (event) {
                is RoomDetailsEvent.LeaveRoom -> {
                    leaveRoomState.eventSink(LeaveRoomEvent.LeaveRoom(room.roomId, needsConfirmation = event.needsConfirmation))
                }
                RoomDetailsEvent.MuteNotification -> {
                    scope.launch(dispatchers.io) {
                        notificationSettingsService.muteRoom(room.roomId)
                    }
                }
                RoomDetailsEvent.UnmuteNotification -> {
                    scope.launch(dispatchers.io) {
                        notificationSettingsService.unmuteRoom(room.roomId, isEncrypted, room.isOneToOne)
                    }
                }
                is RoomDetailsEvent.SetFavorite -> scope.setFavorite(event.isFavorite)
                is RoomDetailsEvent.CopyToClipboard -> {
                    clipboardHelper.copyPlainText(event.text)
                    snackbarDispatcher.post(SnackbarMessage(CommonStrings.common_copied_to_clipboard))
                }
            }
        }

        val roomMemberDetailsState = roomMemberDetailsPresenter?.present()

        val hasMemberVerificationViolations by produceState(false) {
            room.roomMemberIdentityStateChange(waitForEncryption = true)
                .onEach { identities -> value = identities.any { it.identityState == IdentityState.VerificationViolation } }
                .launchIn(this)
        }

        val canReportRoom by produceState(false) { value = client.canReportRoom() }

        return RoomDetailsState(
            roomId = room.roomId,
            roomName = roomName,
            roomAlias = canonicalAlias,
            roomAvatarUrl = roomAvatar,
            roomTopic = topicState,
            memberCount = joinedMemberCount,
            isEncrypted = isEncrypted,
            canInvite = permissions.canInvite,
            canEdit = roomType == RoomDetailsType.Room && permissions.editDetailsPermissions.hasAny,
            roomCallState = roomCallState,
            roomType = roomType,
            roomMemberDetailsState = roomMemberDetailsState,
            leaveRoomState = leaveRoomState,
            roomNotificationSettings = roomNotificationSettingsState.roomNotificationSettings(),
            isFavorite = isFavorite,
            displayRolesAndPermissionsSettings = !isDm && permissions.canEditRolesAndPermissions,
            isPublic = joinRule == JoinRule.Public,
            heroes = roomInfo.heroes.toImmutableList(),
            pinnedMessagesCount = pinnedMessagesCount,
            snackbarMessage = snackbarMessage,
            canShowKnockRequests = canShowKnockRequests,
            knockRequestsCount = knockRequestsCount,
            canShowSecurityAndPrivacy = canShowSecurityAndPrivacy,
            hasMemberVerificationViolations = hasMemberVerificationViolations,
            canReportRoom = canReportRoom,
            isTombstoned = roomInfo.successorRoom != null,
            showDebugInfo = isDeveloperModeEnabled,
            roomVersion = roomInfo.roomVersion,
            eventSink = ::handleEvent,
        )
    }

    @Composable
    private fun roomMemberDetailsPresenter(dmMemberState: RoomMember?) = remember(dmMemberState) {
        dmMemberState?.let { roomMember ->
            roomMembersDetailsPresenterFactory.create(roomMember.userId)
        }
    }

    @Composable
    private fun getRoomType(
        dmMember: RoomMember?,
        currentMember: RoomMember?,
    ): RoomDetailsType = remember(dmMember, currentMember) {
        if (dmMember != null && currentMember != null) {
            RoomDetailsType.Dm(
                me = currentMember,
                otherMember = dmMember,
            )
        } else {
            RoomDetailsType.Room
        }
    }

    private data class Permissions(
        val canInvite: Boolean = false,
        val editDetailsPermissions: RoomDetailsEditPermissions = RoomDetailsEditPermissions.DEFAULT,
        val knockRequestsPermissions: KnockRequestPermissions = KnockRequestPermissions.DEFAULT,
        val securityAndPrivacyPermissions: SecurityAndPrivacyPermissions = SecurityAndPrivacyPermissions.DEFAULT,
        val canEditRolesAndPermissions: Boolean = false,
    )

    @Composable
    private fun getPermissions(): State<Permissions> {
        return room.permissionsAsState(Permissions()) { perms ->
            Permissions(
                canInvite = perms.canOwnUserInvite(),
                editDetailsPermissions = perms.roomDetailsEditPermissions(),
                knockRequestsPermissions = perms.knockRequestPermissions(),
                canEditRolesAndPermissions = perms.canEditRolesAndPermissions(),
                securityAndPrivacyPermissions = perms.securityAndPrivacyPermissions(),
            )
        }
    }

    private fun CoroutineScope.observeNotificationSettings() {
        notificationSettingsService.notificationSettingsChangeFlow.onEach {
            room.updateRoomNotificationSettings()
        }.launchIn(this)
    }

    private fun CoroutineScope.setFavorite(isFavorite: Boolean) = launch {
        room.setIsFavorite(isFavorite)
            .onSuccess {
                analyticsService.captureInteraction(Interaction.Name.MobileRoomFavouriteToggle)
            }
    }
}
