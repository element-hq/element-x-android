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
import io.element.android.tests.testutils.simulateLongTask

class FakeSessionWellknownRetriever(
    private val getWellKnownResult: () -> WellKnown? = { null },
    private val getElementWellKnownResult: () -> ElementWellKnown? = { null },
) : SessionWellknownRetriever {
    override suspend fun getWellKnown(): WellKnown? = simulateLongTask {
        getWellKnownResult()
    }

    override suspend fun getElementWellKnown(): ElementWellKnown? = simulateLongTask {
        getElementWellKnownResult()
    }
}
