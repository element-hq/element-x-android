/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.rolesandpermissions.permissions

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.RoomScope
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class ChangeRoomPermissionsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ChangeRoomPermissionsPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    @Parcelize
    data class Inputs(
        val section: ChangeRoomPermissionsSection,
    ) : NodeInputs, Parcelable

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.section)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        ChangeRoomPermissionsView(
            modifier = modifier,
            state = state,
            onBackClick = this::navigateUp,
        )
    }
}

@Parcelize
enum class ChangeRoomPermissionsSection : Parcelable {
    RoomDetails,
    MessagesAndContent,
    MembershipModeration,
}
