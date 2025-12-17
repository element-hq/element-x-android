/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.runtime.Composable
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class KonsistFlowTest {
    @Test
    fun `flow must be remembered when it is collected as state`() {
        // Match
        // ```).collectAsState```
        // and
        // ```)
        //     .collectAsState```
        val regex = "(.*)\\)(\n\\s*)*\\.collectAsState".toRegex()

        Konsist
            .scopeFromProject()
            .functions()
            .withAnnotationOf(Composable::class)
            .assertFalse(
                additionalMessage = "Please check that the flow is remembered when it is collected as state." +
                    " Only val flows can be not remembered.",
            ) { function ->
                regex.matches(function.text)
            }
    }
}
