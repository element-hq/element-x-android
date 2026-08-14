/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId

interface CreateRoomEntryPoint : FeatureEntryPoint {
    /**
     * Configures the create-room flow before it is started, since it serves rooms and spaces from the same screens.
     */
    interface Builder {
        /**
         * @param isSpace true to create a space rather than a room.
         */
        fun setIsSpace(isSpace: Boolean): Builder

        /**
         * @param parentSpaceId the space the new room should be added to once created.
         */
        fun setParentSpace(parentSpaceId: RoomId): Builder

        /** Builds the node with the options configured so far. */
        fun build(): Node
    }

    fun builder(parentNode: Node, buildContext: BuildContext, callback: Callback): Builder

    interface Callback : Plugin {
        fun onRoomCreated(roomId: RoomId)
    }
}
