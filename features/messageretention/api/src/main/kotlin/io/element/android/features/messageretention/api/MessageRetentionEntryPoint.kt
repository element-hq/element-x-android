/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Copyright 2026 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messageretention.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint

/**
 * Entry point to the (Element Pro) per-room message retention screen.
 *
 * The FOSS build binds a no-op implementation; enterprise builds replace it with the real flow.
 */
interface MessageRetentionEntryPoint : FeatureEntryPoint {
    interface Callback : Plugin {
        fun onDone()
    }

    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
        callback: Callback,
    ): Node
}
