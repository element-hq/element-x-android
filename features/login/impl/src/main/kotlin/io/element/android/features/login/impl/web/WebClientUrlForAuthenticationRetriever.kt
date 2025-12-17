/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.web

import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.wellknown.api.WellknownRetriever
import timber.log.Timber

interface WebClientUrlForAuthenticationRetriever {
    suspend fun retrieve(homeServerUrl: String): String
}

@ContributesBinding(AppScope::class)
class DefaultWebClientUrlForAuthenticationRetriever(
    private val wellknownRetriever: WellknownRetriever,
) : WebClientUrlForAuthenticationRetriever {
    override suspend fun retrieve(homeServerUrl: String): String {
        if (homeServerUrl != AuthenticationConfig.MATRIX_ORG_URL) {
            Timber.w("Temporary account creation flow is only supported on matrix.org")
            throw AccountCreationNotSupported()
        }
        val wellknown = wellknownRetriever.getElementWellKnown(homeServerUrl).dataOrNull()
            ?: throw AccountCreationNotSupported()
        val registrationHelperUrl = wellknown.registrationHelperUrl
        return if (registrationHelperUrl != null) {
            registrationHelperUrl.toUri()
                .buildUpon()
                .appendQueryParameter("hs_url", homeServerUrl)
                .build()
                .toString()
        } else {
            throw AccountCreationNotSupported()
        }
    }
}
