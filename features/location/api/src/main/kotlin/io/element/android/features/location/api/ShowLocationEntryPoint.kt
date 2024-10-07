/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint
import io.element.android.libraries.architecture.NodeInputs

interface ShowLocationEntryPoint : FeatureEntryPoint {
    data class Inputs(val location: Location, val description: String?) : NodeInputs

    fun createNode(parentNode: Node, buildContext: BuildContext, inputs: Inputs): Node
}
