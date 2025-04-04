/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.runtime.Composable
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAnnotationOf
import com.lemonappdev.konsist.api.verify.assertTrue
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

        val allowedMethods = listOf(
            "accountProviderDataSource.flow()",
            "timeline.paginationStatus",
            "analyticsService.getUserConsent()",
        )
        Konsist
            .scopeFromProject()
            .functions()
            .withAnnotationOf(Composable::class)
            .assertTrue(
                additionalMessage = "Please check that the flow is remembered when it is collected as state." +
                    " If the returned flow is backed up by a val, you can add an exception to this test.",
            ) { function ->
                val result = regex.find(function.text) ?: return@assertTrue true
                val functionLine = result.groups[0]?.value
                allowedMethods.any { functionLine?.contains(it) == true }
            }
    }
}
