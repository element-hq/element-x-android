/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.node

import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.EmptyNodeView
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.architecture.NodeFactoriesBindings
import io.element.android.libraries.di.DependencyInjectionGraphOwner
import kotlin.reflect.KClass

/**
 * A parent Node that can create a single type of child Node using the provided factory.
 * This is useful to test a Feature entry point, by providing a fake parent that can create a
 * child Node.
 */
class TestParentNode<Child : Node>(
    private val childNodeClass: KClass<out Node>,
    private val childNodeFactory: (buildContext: BuildContext, plugins: List<Plugin>) -> Child,
) : DependencyInjectionGraphOwner,
    Node(
        buildContext = BuildContext.Companion.root(savedStateMap = null),
        plugins = emptyList(),
        view = EmptyNodeView,
    ) {
    override val graph: NodeFactoriesBindings = NodeFactoriesBindings {
        mapOf(
            childNodeClass to AssistedNodeFactory { buildContext, plugins ->
                childNodeFactory(buildContext, plugins)
            }
        )
    }

    companion object {
        // Inline factory function with reified type parameter
        inline fun <reified Child : Node> create(
            noinline childNodeFactory: (buildContext: BuildContext, plugins: List<Plugin>) -> Child,
        ): TestParentNode<Child> {
            return TestParentNode(Child::class, childNodeFactory)
        }
    }
}
