/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellKnown
import io.element.android.libraries.wellknown.api.WellknownRetriever
import io.element.android.tests.testutils.simulateLongTask

class FakeWellknownRetriever(
    private val getWellKnownResult: (String) -> WellKnown? = { null },
    private val getElementWellKnownResult: (String) -> ElementWellKnown? = { null },
) : WellknownRetriever {
    override suspend fun getWellKnown(baseUrl: String): WellKnown? = simulateLongTask {
        getWellKnownResult(baseUrl)
    }

    override suspend fun getElementWellKnown(baseUrl: String): ElementWellKnown? = simulateLongTask {
        getElementWellKnownResult(baseUrl)
    }
}
