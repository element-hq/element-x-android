/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.verifysession.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.matrix.api.verification.SessionVerificationRequestDetails

interface IncomingVerificationEntryPoint : FeatureEntryPoint {
    data class Params(
        val sessionVerificationRequestDetails: SessionVerificationRequestDetails,
    ) : NodeInputs

    fun nodeBuilder(parentNode: Node, buildContext: BuildContext): NodeBuilder

    interface NodeBuilder {
        fun callback(callback: Callback): NodeBuilder
        fun params(params: Params): NodeBuilder
        fun build(): Node
    }

    interface Callback : Plugin {
        fun onDone()
    }
}
