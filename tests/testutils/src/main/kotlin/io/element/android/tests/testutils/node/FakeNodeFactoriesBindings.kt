/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.node

import com.bumble.appyx.core.node.Node
import io.element.android.libraries.architecture.AssistedNodeFactory
import io.element.android.libraries.architecture.NodeFactoriesBindings
import kotlin.reflect.KClass

class FakeNodeFactoriesBindings(
    private val nodeFactories: Map<KClass<out Node>, AssistedNodeFactory<*>>,
) : NodeFactoriesBindings {
    override fun nodeFactories() = nodeFactories
}
