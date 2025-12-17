/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.test

import io.element.android.features.login.api.LoginIntentResolver
import io.element.android.features.login.api.LoginParams
import io.element.android.tests.testutils.lambda.lambdaError

class FakeLoginIntentResolver(
    private val parseResult: (String) -> LoginParams? = { lambdaError() }
) : LoginIntentResolver {
    override fun parse(uriString: String): LoginParams? {
        return parseResult(uriString)
    }
}
