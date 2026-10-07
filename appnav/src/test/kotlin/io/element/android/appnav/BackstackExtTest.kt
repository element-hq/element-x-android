/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav

import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BackstackExtTest {
    @Test
    fun `replaceRoot without element on top replaces the root`() {
        val backStack = BackStack(initialElement = "A", savedStateMap = null)
        backStack.replaceRoot("D")
        assertThat(backStack.navTargets()).containsExactly("D")
        assertThat(backStack.activeTarget()).isEqualTo("D")
    }

    @Test
    fun `replaceRoot with elements on top replaces the root and keeps the elements on top`() {
        val backStack = BackStack(initialElement = "A", savedStateMap = null)
        backStack.push("B")
        backStack.push("C")
        backStack.replaceRoot("D")
        assertThat(backStack.navTargets()).containsExactly("D", "B", "C").inOrder()
        assertThat(backStack.activeTarget()).isEqualTo("C")
        // When the elements on top are removed, the new root is displayed
        backStack.pop()
        backStack.pop()
        assertThat(backStack.activeTarget()).isEqualTo("D")
    }

    @Test
    fun `replaceRoot with the current root does nothing`() {
        val backStack = BackStack(initialElement = "A", savedStateMap = null)
        backStack.push("B")
        backStack.replaceRoot("A")
        assertThat(backStack.navTargets()).containsExactly("A", "B").inOrder()
        assertThat(backStack.activeTarget()).isEqualTo("B")
    }

    private fun BackStack<String>.navTargets() = elements.value
        .filter { it.targetState != BackStack.State.DESTROYED }
        .map { it.key.navTarget }

    private fun BackStack<String>.activeTarget() = elements.value
        .single { it.targetState == BackStack.State.ACTIVE }
        .key
        .navTarget
}
