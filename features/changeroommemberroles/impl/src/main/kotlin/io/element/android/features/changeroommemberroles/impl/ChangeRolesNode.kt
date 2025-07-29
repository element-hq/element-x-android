/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.changeroommemberroles.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesEntryPoint
import io.element.android.features.changeroommemberroes.api.ChangeRoomMemberRolesListType
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.RoomMember
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class ChangeRolesNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ChangeRolesPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val listType: ChangeRoomMemberRolesListType,
    ) : NodeInputs

    private val inputs: Inputs = inputs()

    private val callback = plugins<ChangeRoomMemberRolesEntryPoint.Callback>()

    private val presenter = presenterFactory.run {
        val role = when (inputs.listType) {
            is ChangeRoomMemberRolesListType.Admins -> RoomMember.Role.Admin
            is ChangeRoomMemberRolesListType.Moderators -> RoomMember.Role.Moderator
            is ChangeRoomMemberRolesListType.SelectNewOwnersWhenLeaving -> RoomMember.Role.Owner(isCreator = false)
        }
        create(role)
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ChangeRolesView(
            modifier = modifier,
            state = state,
            navigateUp = this::navigateUp,
        )
    }
}
