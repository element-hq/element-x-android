/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.architecture

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

/**
 * This interface represents an entrypoint to a feature. Should be used to return the entrypoint node of the feature without exposing the internal types.
 */
interface FeatureEntryPoint

/**
 * Can be used when the feature only exposes a simple node without the need of plugins.
 */
fun interface SimpleFeatureEntryPoint : FeatureEntryPoint {
    fun createNode(parentNode: Node, buildContext: BuildContext): Node
}
