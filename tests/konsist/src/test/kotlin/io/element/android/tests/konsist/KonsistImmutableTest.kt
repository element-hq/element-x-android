/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.runtime.Immutable
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.ext.list.withNameEndingWith
import com.lemonappdev.konsist.api.ext.list.withoutName
import com.lemonappdev.konsist.api.verify.assertEmpty
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class KonsistImmutableTest {
    /**
     * toPersistentList() returns a PersistentList which allow mutations, while toImmutableList() returns
     * an ImmutableList which does not allow mutations. Generally, we do not use the mutation features,
     * so we should prefer toImmutableList.
     */
    @Test
    fun `toPersistentList() should not be used instead of toImmutableList()`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withoutName("toPersistentList() should not be used instead of toImmutableList()")
            .assertFalse(additionalMessage = "Please use toImmutableList() instead of toPersistentList()") {
                it.text.contains(".toPersistentList()")
            }
    }

    /**
     * toPersistentSet() returns a PersistentSet which allow mutations, while toImmutableSet() returns
     * an ImmutableSet which does not allow mutations. Generally, we do not use the mutation features,
     * so we should prefer toImmutableSet.
     */
    @Test
    fun `toPersistentSet() should not be used instead of toImmutableSet()`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withoutName("toPersistentSet() should not be used instead of toImmutableSet()")
            .assertFalse(additionalMessage = "Please use toImmutableSet() instead of toPersistentSet()") {
                it.text.contains(".toPersistentSet()")
            }
    }

    /**
     * toPersistentMap() returns a PersistentMap which allow mutations, while toImmutableMap() returns
     * an ImmutableMap which does not allow mutations. Generally, we do not use the mutation features,
     * so we should prefer toImmutableMap.
     */
    @Test
    fun `toPersistentMap() should not be used instead of toImmutableMap()`() {
        Konsist
            .scopeFromProject()
            .functions()
            .withoutName("toPersistentMap() should not be used instead of toImmutableMap()")
            .assertFalse(additionalMessage = "Please use toImmutableMap() instead of toPersistentMap()") {
                it.text.contains(".toPersistentMap()")
            }
    }

    @Test
    fun `Immutable annotation is not used on sealed interface for Presenter Events`() {
        Konsist
            .scopeFromProduction()
            .interfaces()
            .withNameEndingWith("Events")
            .withAnnotationOf(Immutable::class)
            .assertEmpty()
    }
}
