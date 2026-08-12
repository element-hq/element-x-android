/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.wellknown.test

import io.element.android.libraries.matrix.api.UrlContentFetcher
import io.element.android.libraries.wellknown.api.WellknownRetriever

class FakeWellknownRetrieverFactory(
    private val wellknownRetriever: WellknownRetriever = FakeWellknownRetriever(),
) : WellknownRetriever.Factory {
    override fun create(urlContentFetcher: UrlContentFetcher): WellknownRetriever = wellknownRetriever
}
