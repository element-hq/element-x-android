/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.web

import android.net.Uri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.resolver.network.WellknownAPI
import io.element.android.features.login.impl.screens.createaccount.AccountCreationNotSupported
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.network.RetrofitFactory
import java.net.HttpURLConnection
import timber.log.Timber
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
        } catch (e: Exception) {
            throw when {
                e is retrofit2.HttpException &&
                    e.code() == HttpURLConnection.HTTP_NOT_FOUND -> AccountCreationNotSupported()
                else -> e
            }
        }
        val registrationHelperUrl = result.registrationHelperUrl
        return if (registrationHelperUrl != null) {
            Uri.parse(registrationHelperUrl)
                .buildUpon()
                .appendQueryParameter("hs_url", homeServerUrl)
                .build()
                .toString()
        } else {
            throw AccountCreationNotSupported()
        }
    }
}
