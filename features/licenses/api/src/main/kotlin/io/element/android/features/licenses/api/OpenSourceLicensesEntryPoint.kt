/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.api

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

interface OpenSourceLicensesEntryPoint {
    fun getNode(node: Node, buildContext: BuildContext): Node
}
