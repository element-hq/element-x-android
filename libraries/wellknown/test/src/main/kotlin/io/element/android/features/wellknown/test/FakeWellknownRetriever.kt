/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.EnterpriseRemoteConfigSource
import io.element.android.libraries.wellknown.api.WellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

class FakeWellknownRetriever(
    private val getElementWellKnownResult: (String, EnterpriseRemoteConfigSource) -> WellknownRetrieverResult<ElementWellKnown> = { _, _ ->
        WellknownRetrieverResult.NotFound
    },
) : WellknownRetriever {
    override suspend fun getElementWellKnown(host: String, source: EnterpriseRemoteConfigSource): WellknownRetrieverResult<ElementWellKnown> {
        return getElementWellKnownResult(host, source)
    }
}
