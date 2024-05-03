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

package io.element.android.features.roomdetails.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.poll.api.history.PollHistoryEntryPoint
import io.element.android.features.roomdetails.api.RoomDetailsEntryPoint
import io.element.android.features.roomdetails.impl.edit.RoomDetailsEditNode
import io.element.android.features.roomdetails.impl.invite.RoomInviteMembersNode
import io.element.android.features.roomdetails.impl.members.RoomMemberListNode
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsNode
import io.element.android.features.roomdetails.impl.notificationsettings.RoomNotificationSettingsNode
import io.element.android.features.roomdetails.impl.rolesandpermissions.RolesAndPermissionsFlowNode
import io.element.android.features.userprofile.shared.UserProfileNodeHelper
import io.element.android.features.userprofile.shared.avatar.AvatarPreviewNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.mediaviewer.api.local.MediaInfo
import io.element.android.libraries.mediaviewer.api.viewer.MediaViewerNode
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class RoomDetailsFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val pollHistoryEntryPoint: PollHistoryEntryPoint,
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
        data object AdminSettings : NavTarget
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
                        backstack.push(NavTarget.AvatarPreview(name, url))
                    }

                    override fun openPollHistory() {
                        backstack.push(NavTarget.PollHistory)
                    }

                    override fun openAdminSettings() {
                        backstack.push(NavTarget.AdminSettings)
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
                        backstack.push(NavTarget.AvatarPreview(username, avatarUrl))
                    }

                    override fun onStartDM(roomId: RoomId) {
                        plugins<RoomDetailsEntryPoint.Callback>().forEach { it.onOpenRoom(roomId) }
                    }
                }
                val plugins = listOf(RoomMemberDetailsNode.RoomMemberDetailsInput(navTarget.roomMemberId), callback)
                createNode<RoomMemberDetailsNode>(buildContext, plugins)
            }
            is NavTarget.AvatarPreview -> {
                // We need to fake the MimeType here for the viewer to work.
                val mimeType = MimeTypes.Images
                val input = MediaViewerNode.Inputs(
                    mediaInfo = MediaInfo(
                        name = navTarget.name,
                        mimeType = mimeType,
                        formattedFileSize = "",
                        fileExtension = ""
                    ),
                    mediaSource = MediaSource(url = navTarget.avatarUrl),
                    thumbnailSource = null,
                    canDownload = false,
                    canShare = false,
                )
                createNode<AvatarPreviewNode>(buildContext, listOf(input))
            }

            is NavTarget.PollHistory -> {
                pollHistoryEntryPoint.createNode(this, buildContext)
            }

            is NavTarget.AdminSettings -> {
                createNode<RolesAndPermissionsFlowNode>(buildContext)
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
