/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.node

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.navigation.model.permanent.PermanentNavModel
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.ParentNode
import com.bumble.appyx.core.node.node
import io.element.android.libraries.di.DependencyInjectionGraphOwner

/**
 * A Node creates its children using the dependency injection graph of its ancestors, so a Node under test
 * needs such a parent to be able to create its children.
 */
class FakeParentNode(
    override val graph: Any,
) : ParentNode<Unit>(
    navModel = PermanentNavModel(navTargets = setOf(Unit), savedStateMap = null),
    buildContext = BuildContext.root(savedStateMap = null),
),
    DependencyInjectionGraphOwner {
    override fun resolve(navTarget: Unit, buildContext: BuildContext): Node = node(buildContext) {}

    @Composable
    override fun View(modifier: Modifier) = Unit
}
