/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.test

import android.content.Intent
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcIntentResolver
import io.element.android.tests.testutils.lambda.lambdaError

class FakeOidcIntentResolver(
    private val resolveResult: (Intent) -> OidcAction? = { lambdaError() }
) : OidcIntentResolver {
    override fun resolve(intent: Intent): OidcAction? {
        return resolveResult(intent)
    }
}
