/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.createroom.impl.addpeople.AddPeopleNode
import io.element.android.features.createroom.impl.configureroom.ConfigureRoomNode
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.parcelize.Parcelize

@ContributesNode(SessionScope::class)
class CreateRoomFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : BaseFlowNode<CreateRoomFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.ConfigureRoom,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.ConfigureRoom -> createNode<ConfigureRoomNode>(buildContext)
            is NavTarget.AddPeople -> createNode<AddPeopleNode>(buildContext)
        }
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object ConfigureRoom : NavTarget

        @Parcelize
        data class AddPeople(val roomId: RoomId) : NavTarget
    }
}
