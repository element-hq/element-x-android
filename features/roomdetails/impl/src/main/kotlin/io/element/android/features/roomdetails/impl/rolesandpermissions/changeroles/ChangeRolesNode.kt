/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.changeroles

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
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
    sealed interface ListType : Parcelable {
        @Parcelize
        data object Admins : ListType
        @Parcelize
        data object Moderators : ListType
    }

    @Parcelize
    data class Inputs(
        val listType: ListType,
    ) : NodeInputs, Parcelable

    private val inputs: Inputs = inputs()

    private val presenter = presenterFactory.run {
        val role = when (inputs.listType) {
            is ListType.Admins -> RoomMember.Role.ADMIN
            is ListType.Moderators -> RoomMember.Role.MODERATOR
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
