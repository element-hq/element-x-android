/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.signedout.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.FeatureEntryPoint

interface SignedOutEntryPoint : FeatureEntryPoint {
    /**
     * Create a node which displays the "signed out" screen for every session whose token has become invalid, active or not,
     * one after the other. It renders nothing when there is no signed out session, so it is meant to be always attached,
     * on top of the rest of the app.
     */
    fun createNode(
        parentNode: Node,
        buildContext: BuildContext,
    ): Node
}
