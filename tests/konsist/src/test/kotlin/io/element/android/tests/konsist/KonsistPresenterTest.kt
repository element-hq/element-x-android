/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.constructors
import com.lemonappdev.konsist.api.ext.list.withAllParentsOf
import com.lemonappdev.konsist.api.verify.assertTrue
import io.element.android.libraries.architecture.Presenter
import org.junit.Test

class KonsistPresenterTest {
    @Test
    fun `'Presenter' should not depend on other presenters`() {
        Konsist.scopeFromProject()
            .classes()
            .withAllParentsOf(Presenter::class)
            .constructors
            .assertTrue { constructor ->
                val result = constructor.parameters.none { parameter ->
                    parameter.type.name.endsWith("Presenter")
                }
                result
            }
    }
}
