/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellKnown
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultSessionWellknownRetriever @Inject constructor(
    private val matrixClient: MatrixClient,
    private val parser: Json,
) : SessionWellknownRetriever {
    private val domain by lazy { matrixClient.userIdServerName() }

    override suspend fun getWellKnown(): WellKnown? {
        val url = "https://$domain/.well-known/matrix/client"
        return matrixClient
            .getUrl(url)
            .mapCatchingExceptions {
                val data = String(it)
                parser.decodeFromString(InternalWellKnown.serializer(), data)
            }
            .onFailure { Timber.e(it, "Failed to retrieve .well-known from $domain") }
            .map { it.map() }
            .getOrNull()
    }

    override suspend fun getElementWellKnown(): ElementWellKnown? {
        val url = "https://$domain/.well-known/element/element.json"
        return matrixClient
            .getUrl(url)
            .mapCatchingExceptions {
                val data = String(it)
                parser.decodeFromString(InternalElementWellKnown.serializer(), data)
            }
            .onFailure { Timber.e(it, "Failed to retrieve Element .well-known from $domain") }
            .map { it.map() }
            .getOrNull()
    }
}
