/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.mapCatchingExceptions
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.exception.ClientException
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.SessionWellknownRetriever
import io.element.android.libraries.wellknown.api.WellknownRetrieverResult
import timber.log.Timber

@ContributesBinding(SessionScope::class)
class DefaultSessionWellknownRetriever(
    private val matrixClient: MatrixClient,
    private val json: JsonProvider,
) : SessionWellknownRetriever {
    private val domain by lazy { matrixClient.userIdServerName() }

    override suspend fun getElementWellKnown(): WellknownRetrieverResult<ElementWellKnown> {
        val url = "https://$domain/.well-known/element/element.json"
        return matrixClient
            .getUrl(url)
            .mapCatchingExceptions {
                val data = String(it)
                json().decodeFromString<InternalElementWellKnown>(data).map()
            }
            .toWellknownRetrieverResult()
    }

    private fun <T> Result<T>.toWellknownRetrieverResult(): WellknownRetrieverResult<T> = fold(
        onSuccess = {
            WellknownRetrieverResult.Success(it)
        },
        onFailure = {
            Timber.e(it, "Failed to retrieve Element .well-known from $domain")
            // This check on message value is not ideal but this is what we got from the SDK.
            if ((it as? ClientException.Generic)?.message?.contains("404") == true) {
                WellknownRetrieverResult.NotFound
            } else {
                WellknownRetrieverResult.Error(it as Exception)
            }
        }
    )
}
