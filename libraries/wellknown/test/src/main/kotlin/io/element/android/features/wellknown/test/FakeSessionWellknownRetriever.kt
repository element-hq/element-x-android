/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellKnown
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.simulateLongTask

class FakeSessionWellknownRetriever(
    private val getWellKnownResult: () -> WellknownRetrieverResult<WellKnown> = { WellknownRetrieverResult.NotFound },
    private val getElementWellKnownResult: () -> WellknownRetrieverResult<ElementWellKnown> = { WellknownRetrieverResult.NotFound },
) : SessionWellknownRetriever {
    override suspend fun getWellKnown(): WellknownRetrieverResult<WellKnown> = simulateLongTask {
        getWellKnownResult()
    }

    override suspend fun getElementWellKnown(): WellknownRetrieverResult<ElementWellKnown> = simulateLongTask {
        getElementWellKnownResult()
    }
}
