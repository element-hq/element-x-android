/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.api.create

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint

interface CreatePollEntryPoint : FeatureEntryPoint {
    data class Params(
        val mode: CreatePollMode,
    )

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun params(params: Params): NodeBuilder
        fun build(): Node
    }
}
