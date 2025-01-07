/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import kotlinx.parcelize.Parcelize

interface RoomDetailsEntryPoint : FeatureEntryPoint {
    sealed interface InitialTarget : Parcelable {
        @Parcelize
        data object RoomDetails : InitialTarget

        @Parcelize
        data class RoomMemberDetails(val roomMemberId: UserId) : InitialTarget

        @Parcelize
        data object RoomNotificationSettings : InitialTarget
    }

    data class Params(val initialElement: InitialTarget) : NodeInputs

    interface Callback : Plugin {
        fun onOpenGlobalNotificationSettings()
        fun onOpenRoom(roomId: RoomId)
        fun onPermalinkClick(data: PermalinkData, pushToBackstack: Boolean)
        fun onForwardedToSingleRoom(roomId: RoomId)
    }

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun callback(callback: Callback): NodeBuilder
        fun build(): Node
    }

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder
}
