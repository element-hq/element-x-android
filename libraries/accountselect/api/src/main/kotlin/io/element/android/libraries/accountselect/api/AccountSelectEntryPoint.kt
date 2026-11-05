/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.accountselect.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.permalink.PermalinkData

interface AccountSelectEntryPoint : FeatureEntryPoint {
    /**
     * @param permalinkData the permalink which will be opened with the selected account.
     */
    data class Params(
        val permalinkData: PermalinkData,
    )

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        params: Params,
        callback: Callback,
    ): Node

    interface Callback : Plugin {
        fun onAccountSelected(sessionId: SessionId)
        fun onCancel()
    }
}
