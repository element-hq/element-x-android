/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.test

import android.content.Intent
import io.element.android.libraries.oauth.api.OAuthAction
import io.element.android.libraries.oauth.api.OAuthIntentResolver
import io.element.android.tests.testutils.lambda.lambdaError

class FakeOAuthIntentResolver(
    private val resolveResult: (Intent) -> OAuthAction? = { lambdaError() }
) : OAuthIntentResolver {
    override fun resolve(intent: Intent): OAuthAction? {
        return resolveResult(intent)
    }
}
