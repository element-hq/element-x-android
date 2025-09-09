/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.analytics.impl

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.analytics.api.AnalyticsEntryPoint
import io.element.android.libraries.architecture.createNode

@ContributesBinding(AppScope::class)
@Inject class DefaultAnalyticsEntryPoint : AnalyticsEntryPoint {
    override fun createNode(parentNode: Node, buildContext: BuildContext): Node {
        return parentNode.createNode<AnalyticsOptInNode>(buildContext)
    }
}
