/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withParameter
import com.lemonappdev.konsist.api.verify.assertEmpty
import org.junit.Test

class KonsistParameterNameTest {
    @Test
    fun `Function parameter should not end with 'Press' but with 'Click'`() {
        Konsist.scopeFromProject()
            .functions()
            .withParameter { parameter ->
                parameter.name.endsWith("Press")
            }
            .assertEmpty(additionalMessage = "Please rename the parameter, for instance from 'onBackPress' to 'onBackClick'.")
    }
}
