/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.web

import androidx.core.net.toUri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.resolver.network.WellknownAPI
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import timber.log.Timber
import java.net.HttpURLConnection
import javax.inject.Inject

interface WebClientUrlForAuthenticationRetriever {
    suspend fun retrieve(homeServerUrl: String): String
}

@ContributesBinding(AppScope::class)
class DefaultWebClientUrlForAuthenticationRetriever @Inject constructor(
    private val retrofitFactory: RetrofitFactory,
) : WebClientUrlForAuthenticationRetriever {
    override suspend fun retrieve(homeServerUrl: String): String {
        if (homeServerUrl != AuthenticationConfig.MATRIX_ORG_URL) {
            Timber.w("Temporary account creation flow is only supported on matrix.org")
            throw AccountCreationNotSupported()
        }
        val wellknownApi = retrofitFactory.create(homeServerUrl)
            .create(WellknownAPI::class.java)
        val result = try {
            wellknownApi.getElementWellKnown()
        } catch (e: retrofit2.HttpException) {
            throw when {
                e.code() == HttpURLConnection.HTTP_NOT_FOUND -> AccountCreationNotSupported()
                else -> e
            }
        }
        val registrationHelperUrl = result.registrationHelperUrl
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
