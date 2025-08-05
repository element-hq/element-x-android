/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.accesscontrol

import io.element.android.features.login.impl.resolver.network.ElementWellKnown
import io.element.android.tests.testutils.simulateLongTask

class FakeElementWellknownRetriever(
    private val retrieveResult: (String) -> ElementWellKnown? = { null },
) : ElementWellknownRetriever {
    override suspend fun retrieve(accountProviderUrl: String): ElementWellKnown? = simulateLongTask {
        retrieveResult(accountProviderUrl)
    }
}
