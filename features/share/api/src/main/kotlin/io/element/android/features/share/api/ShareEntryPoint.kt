/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

interface ShareEntryPoint : FeatureEntryPoint {
    /**
     * @param sessionId the session to use initially. The user can select another session.
     * @param shareIntentData the data to share.
     */
    data class Params(
        val sessionId: SessionId,
        val shareIntentData: ShareIntentData,
    )

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        /**
         * Called when the data has been shared.
         * @param sessionId the session which has been used to share the data. It can be different from the current session.
         * @param roomIds the rooms the data has been shared to.
         */
        fun onDone(sessionId: SessionId, roomIds: List<RoomId>)

        /**
         * Called when the user cancels the share.
         */
        fun onCancel()
    }
}
