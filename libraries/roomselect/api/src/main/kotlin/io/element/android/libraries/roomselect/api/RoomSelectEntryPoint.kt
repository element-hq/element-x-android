/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.roomselect.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

interface RoomSelectEntryPoint : FeatureEntryPoint {
    /**
     * @param sessionId the session to use initially. In [RoomSelectMode.Share] mode, the user can select another session.
     * @param mode the mode of the screen.
     * @param maxNumberOfRooms the maximum number of rooms the user can select.
     */
    data class Params(
        val sessionId: SessionId,
        val mode: RoomSelectMode,
        val maxNumberOfRooms: Int,
    )

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        /**
         * @param sessionId the session which has been selected by the user, the rooms belong to this session.
         * @param roomIds the selected rooms.
         */
        fun onRoomSelected(sessionId: SessionId, roomIds: List<RoomId>)
        fun onCancel()
    }

    companion object {
        const val DEFAULT_MAX_NUMBER_OF_ROOMS = 10
    }
}
