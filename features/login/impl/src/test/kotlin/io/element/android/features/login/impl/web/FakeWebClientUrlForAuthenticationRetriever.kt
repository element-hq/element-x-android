/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.web

import io.element.android.tests.testutils.lambda.lambdaError

class FakeWebClientUrlForAuthenticationRetriever(
    private val retrieveLambda: suspend (homeServerUrl: String) -> String = { lambdaError() }
) : WebClientUrlForAuthenticationRetriever {
    override suspend fun retrieve(homeServerUrl: String): String {
        return retrieveLambda(homeServerUrl)
    }
}
