/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.api

import android.os.Parcelable
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import kotlinx.parcelize.Parcelize

interface PreferencesEntryPoint : FeatureEntryPoint {
    sealed interface InitialTarget : Parcelable {
        @Parcelize
        data object Root : InitialTarget

        @Parcelize
        data object NotificationSettings : InitialTarget

        @Parcelize
        data object NotificationTroubleshoot : InitialTarget
    }

    data class Params(val initialElement: InitialTarget) : NodeInputs

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun navigateToAddAccount()
        fun navigateToBugReport()
        fun navigateToSecureBackup()
        fun navigateToRoomNotificationSettings(roomId: RoomId)
        fun navigateToEvent(roomId: RoomId, eventId: EventId)
    }
}
