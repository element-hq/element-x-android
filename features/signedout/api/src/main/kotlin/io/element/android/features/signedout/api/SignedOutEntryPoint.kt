/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.matrix.api.core.SessionId

interface SignedOutEntryPoint : FeatureEntryPoint {
    data class Params(
        val sessionId: SessionId,
    )

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun build(): Node
    }
}
