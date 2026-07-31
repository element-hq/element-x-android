/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellKnownSource
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult

@ContributesBinding(SessionScope::class)
class DefaultSessionWellknownRetriever(
    private val matrixClient: MatrixClient,
    private val wellknownRetriever: WellknownRetriever,
) : SessionWellknownRetriever {
    private val domain by lazy { matrixClient.userIdServerName() }

    override suspend fun getElementWellKnown(source: ElementWellKnownSource): WellknownRetrieverResult<ElementWellKnown> {
        return wellknownRetriever.getElementWellKnown(domain, source)
    }
}
