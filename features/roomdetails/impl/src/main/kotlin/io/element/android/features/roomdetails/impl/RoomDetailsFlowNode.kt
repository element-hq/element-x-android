/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.anvilannotations.ContributesNode
import io.element.android.appconfig.LearnMoreConfig
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.knockrequests.api.list.KnockRequestsListEntryPoint
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.poll.api.history.PollHistoryEntryPoint
import io.element.android.features.reportroom.api.ReportRoomEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.roomdetails.impl.edit.RoomDetailsEditNode
import io.element.android.features.roomdetails.impl.invite.RoomInviteMembersNode
import io.element.android.features.roomdetails.impl.members.RoomMemberListNode
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsNode
import io.element.android.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsNode
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsFlowNode
import io.element.android.features.roomdetails.impl.securityandprivacy.SecurityAndPrivacyFlowNode
import io.element.android.features.userprofile.shared.UserProfileNodeHelper
import io.element.android.features.verifysession.api.OutgoingVerificationEntryPoint
import io.element.android.libraries.architecture.BackstackWithOverlayBox
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.overlay.operation.hide
import io.element.android.libraries.architecture.overlay.operation.show
import io.element.android.libraries.designsystem.utils.OpenUrlInTabView
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.verification.VerificationRequest
import io.element.android.libraries.mediaviewer.api.MediaGalleryEntryPoint
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class RoomDetailsFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val pollHistoryEntryPoint: PollHistoryEntryPoint,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val room: BaseRoom,
    private val analyticsService: AnalyticsService,
    private val messagesEntryPoint: MessagesEntryPoint,
    private val knockRequestsListEntryPoint: KnockRequestsListEntryPoint,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
    private val mediaGalleryEntryPoint: MediaGalleryEntryPoint,
    private val outgoingVerificationEntryPoint: OutgoingVerificationEntryPoint,
    private val reportRoomEntryPoint: ReportRoomEntryPoint,
) : BaseFlowNode<RoomDetailsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance<RoomDetailsEntryPoint.Params>().first().initialElement.toNavTarget(),
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object RoomDetails : NavTarget

        @Parcelize
        data object RoomMemberList : NavTarget

        @Parcelize
        data object RoomDetailsEdit : NavTarget

        @Parcelize
        data object InviteMembers : NavTarget

        @Parcelize
        data class RoomNotificationSettings(
            /**
             * When presented from outside the context of the room, the rooms settings UI is different.
             * Figma designs: https://www.figma.com/file/0MMNu7cTOzLOlWb7ctTkv3/Element-X?type=design&node-id=5199-198932&mode=design&t=fTTvpuxYFjewYQOe-0
             */
            val showUserDefinedSettingStyle: Boolean
        ) : NavTarget

        @Parcelize
        data class RoomMemberDetails(val roomMemberId: UserId) : NavTarget

        @Parcelize
        data class AvatarPreview(val name: String, val avatarUrl: String) : NavTarget

        @Parcelize
        data object PollHistory : NavTarget

        @Parcelize
        data object MediaGallery : NavTarget

        @Parcelize
        data object AdminSettings : NavTarget

        @Parcelize
        data object PinnedMessagesList : NavTarget

        @Parcelize
        data object KnockRequestsList : NavTarget

        @Parcelize
        data object SecurityAndPrivacy : NavTarget

        @Parcelize
        data class VerifyUser(val userId: UserId) : NavTarget

        @Parcelize
        data object ReportRoom : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.RoomDetails -> {
                val roomDetailsCallback = object : RoomDetailsNode.Callback {
                    override fun openRoomMemberList() {
                        backstack.push(NavTarget.RoomMemberList)
                    }

                    override fun editRoomDetails() {
                        backstack.push(NavTarget.RoomDetailsEdit)
                    }

                    override fun openInviteMembers() {
                        backstack.push(NavTarget.InviteMembers)
                    }

                    override fun openRoomNotificationSettings() {
                        backstack.push(NavTarget.RoomNotificationSettings(showUserDefinedSettingStyle = false))
                    }

                    override fun openAvatarPreview(name: String, url: String) {
                        overlay.show(NavTarget.AvatarPreview(name, url))
                    }

                    override fun openPollHistory() {
                        backstack.push(NavTarget.PollHistory)
                    }

                    override fun openMediaGallery() {
                        backstack.push(NavTarget.MediaGallery)
                    }

                    override fun openAdminSettings() {
                        backstack.push(NavTarget.AdminSettings)
                    }

                    override fun openPinnedMessagesList() {
                        backstack.push(NavTarget.PinnedMessagesList)
                    }

                    override fun openKnockRequestsList() {
                        backstack.push(NavTarget.KnockRequestsList)
                    }

                    override fun openSecurityAndPrivacy() {
                        backstack.push(NavTarget.SecurityAndPrivacy)
                    }

                    override fun openDmUserProfile(userId: UserId) {
                        backstack.push(NavTarget.RoomMemberDetails(userId))
                    }

                    override fun onJoinCall() {
                        val inputs = CallType.RoomCall(
                            sessionId = room.sessionId,
                            roomId = room.roomId,
                        )
                        analyticsService.captureInteraction(Interaction.Name.MobileRoomCallButton)
                        elementCallEntryPoint.startCall(inputs)
                    }

                    override fun openReportRoom() {
                        backstack.push(NavTarget.ReportRoom)
                    }
                }
                createNode<RoomDetailsNode>(buildContext, listOf(roomDetailsCallback))
            }

            NavTarget.RoomMemberList -> {
                val roomMemberListCallback = object : RoomMemberListNode.Callback {
                    override fun openRoomMemberDetails(roomMemberId: UserId) {
                        backstack.push(NavTarget.RoomMemberDetails(roomMemberId))
                    }

                    override fun openInviteMembers() {
                        backstack.push(NavTarget.InviteMembers)
                    }
                }
                createNode<RoomMemberListNode>(buildContext, listOf(roomMemberListCallback))
            }

            NavTarget.RoomDetailsEdit -> {
                createNode<RoomDetailsEditNode>(buildContext)
            }

            NavTarget.InviteMembers -> {
                createNode<RoomInviteMembersNode>(buildContext)
            }

            is NavTarget.RoomNotificationSettings -> {
                val input = RoomNotificationSettingsNode.RoomNotificationSettingInput(navTarget.showUserDefinedSettingStyle)
                val callback = object : RoomNotificationSettingsNode.Callback {
                    override fun openGlobalNotificationSettings() {
                        plugins<RoomDetailsEntryPoint.Callback>().forEach { it.onOpenGlobalNotificationSettings() }
                    }
                }
                createNode<RoomNotificationSettingsNode>(buildContext, listOf(input, callback))
            }

            is NavTarget.RoomMemberDetails -> {
                val callback = object : UserProfileNodeHelper.Callback {
                    override fun openAvatarPreview(username: String, avatarUrl: String) {
                        overlay.show(NavTarget.AvatarPreview(username, avatarUrl))
                    }

                    override fun onStartDM(roomId: RoomId) {
                        plugins<RoomDetailsEntryPoint.Callback>().forEach { it.onOpenRoom(roomId) }
                    }

                    override fun onStartCall(dmRoomId: RoomId) {
                        elementCallEntryPoint.startCall(CallType.RoomCall(roomId = dmRoomId, sessionId = room.sessionId))
                    }

                    override fun onVerifyUser(userId: UserId) {
                        backstack.push(NavTarget.VerifyUser(userId))
                    }
                }
                val plugins = listOf(RoomMemberDetailsNode.RoomMemberDetailsInput(navTarget.roomMemberId), callback)
                createNode<RoomMemberDetailsNode>(buildContext, plugins)
            }
            is NavTarget.AvatarPreview -> {
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        overlay.hide()
                    }

                    override fun onViewInTimeline(eventId: EventId) {
                        // Cannot happen
                    }
                }
                mediaViewerEntryPoint.nodeBuilder(this, buildContext)
                    .avatar(
                        navTarget.name,
                        navTarget.avatarUrl,
                    )
                    .callback(callback)
                    .build()
            }
            is NavTarget.PollHistory -> {
                pollHistoryEntryPoint.createNode(this, buildContext)
            }
            is NavTarget.MediaGallery -> {
                val callback = object : MediaGalleryEntryPoint.Callback {
                    override fun onBackClick() {
                        backstack.pop()
                    }

                    override fun onViewInTimeline(eventId: EventId) {
                        val permalinkData = PermalinkData.RoomLink(
                            roomIdOrAlias = room.roomId.toRoomIdOrAlias(),
                            eventId = eventId,
                        )
                        plugins<RoomDetailsEntryPoint.Callback>().forEach {
                            it.onPermalinkClick(permalinkData, pushToBackstack = false)
                        }
                    }
                }
                mediaGalleryEntryPoint.nodeBuilder(this, buildContext)
                    .callback(callback)
                    .build()
            }

            is NavTarget.AdminSettings -> {
                createNode<RolesAndPermissionsFlowNode>(buildContext)
            }
            NavTarget.PinnedMessagesList -> {
                val params = MessagesEntryPoint.Params(
                    MessagesEntryPoint.InitialTarget.PinnedMessages
                )
                val callback = object : MessagesEntryPoint.Callback {
                    override fun onRoomDetailsClick() = Unit

                    override fun onUserDataClick(userId: UserId) = Unit

                    override fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean) {
                        plugins<RoomDetailsEntryPoint.Callback>().forEach { it.onPermalinkClick(data, pushToBackstack) }
                    }

                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        plugins<RoomDetailsEntryPoint.Callback>().forEach { it.onForwardedToSingleRoom(roomId) }
                    }
                }
                return messagesEntryPoint.nodeBuilder(this, buildContext)
                    .params(params)
                    .callback(callback)
                    .build()
            }
            NavTarget.KnockRequestsList -> {
                knockRequestsListEntryPoint.createNode(this, buildContext)
            }
            NavTarget.SecurityAndPrivacy -> {
                createNode<SecurityAndPrivacyFlowNode>(buildContext)
            }
            is NavTarget.VerifyUser -> {
                val params = OutgoingVerificationEntryPoint.Params(
                    showDeviceVerifiedScreen = true,
                    verificationRequest = VerificationRequest.Outgoing.User(userId = navTarget.userId,)
                )
                outgoingVerificationEntryPoint.nodeBuilder(this, buildContext)
                    .params(params)
                    .callback(object : OutgoingVerificationEntryPoint.Callback {
                        override fun onDone() {
                            backstack.pop()
                        }

                        override fun onBack() {
                            backstack.pop()
                        }

                        override fun onLearnMoreAboutEncryption() {
                            learnMoreUrl.value = LearnMoreConfig.ENCRYPTION_URL
                        }
                    })
                    .build()
            }
            is NavTarget.ReportRoom -> {
                reportRoomEntryPoint.createNode(this, buildContext, room.roomId)
            }
        }
    }

    private val learnMoreUrl = mutableStateOf<String?>(null)

    @Composable
    override fun View(modifier: Modifier) {
        BackstackWithOverlayBox(modifier)

        OpenUrlInTabView(learnMoreUrl)
    }
}
