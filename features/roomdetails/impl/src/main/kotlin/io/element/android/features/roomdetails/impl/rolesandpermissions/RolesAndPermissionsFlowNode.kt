/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles.ChangeRolesNode
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsNode
import io.element.android.features.roomdetails.impl.rolesandpermissions.permissions.ChangeRoomPermissionsSection
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class RolesAndPermissionsFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<RolesAndPermissionsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.AdminSettings,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object AdminSettings : NavTarget

        @Parcelize
        data object AdminList : NavTarget

        @Parcelize
        data object ModeratorList : NavTarget

        @Parcelize
        data class ChangeRoomPermissions(val section: ChangeRoomPermissionsSection) : NavTarget
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.AdminSettings -> {
                val callback = object : RolesAndPermissionsNode.Callback {
                    override fun openAdminList() {
                        backstack.push(NavTarget.AdminList)
                    }

                    override fun openModeratorList() {
                        backstack.push(NavTarget.ModeratorList)
                    }

                    override fun openEditRoomDetailsPermissions() {
                        backstack.push(NavTarget.ChangeRoomPermissions(ChangeRoomPermissionsSection.RoomDetails))
                    }

                    override fun openMessagesAndContentPermissions() {
                        backstack.push(NavTarget.ChangeRoomPermissions(ChangeRoomPermissionsSection.MessagesAndContent))
                    }

                    override fun openModerationPermissions() {
                        backstack.push(NavTarget.ChangeRoomPermissions(ChangeRoomPermissionsSection.MembershipModeration))
                    }
                }
                createNode<RolesAndPermissionsNode>(
                    buildContext = buildContext,
                    plugins = listOf(callback),
                )
            }
            is NavTarget.AdminList -> {
                val inputs = ChangeRolesNode.Inputs(ChangeRolesNode.ListType.Admins)
                createNode<ChangeRolesNode>(
                    buildContext = buildContext,
                    plugins = listOf(inputs),
                )
            }
            is NavTarget.ModeratorList -> {
                val inputs = ChangeRolesNode.Inputs(ChangeRolesNode.ListType.Moderators)
                createNode<ChangeRolesNode>(
                    buildContext = buildContext,
                    plugins = listOf(inputs),
                )
            }
            is NavTarget.ChangeRoomPermissions -> {
                val inputs = ChangeRoomPermissionsNode.Inputs(navTarget.section)
                createNode<ChangeRoomPermissionsNode>(
                    buildContext = buildContext,
                    plugins = listOf(inputs),
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView()
    }
}
