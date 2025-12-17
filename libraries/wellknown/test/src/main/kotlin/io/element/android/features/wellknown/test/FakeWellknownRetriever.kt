/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.WellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import io.element.android.tests.testutils.simulateLongTask

class FakeWellknownRetriever(
    private val getElementWellKnownResult: (String) -> WellknownRetrieverResult<ElementWellKnown> = { WellknownRetrieverResult.NotFound },
) : WellknownRetriever {
    override suspend fun getElementWellKnown(baseUrl: String): WellknownRetrieverResult<ElementWellKnown> = simulateLongTask {
        getElementWellKnownResult(baseUrl)
    }
}
