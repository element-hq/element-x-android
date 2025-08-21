/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.licenses.api.OpenSourceLicensesEntryPoint
import io.element.android.libraries.architecture.createNode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultOpenSourcesLicensesEntryPoint() : OpenSourceLicensesEntryPoint {
    override fun getNode(node: Node, buildContext: BuildContext): Node {
        return node.createNode<DependenciesFlowNode>(buildContext)
    }
}
