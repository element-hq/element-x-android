/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.test.accesscontrol

import io.element.android.features.login.api.accesscontrol.AccountProviderAccessControl
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAccountProviderAccessControl(
    private val isAllowedToConnectToAccountProviderResult: (String) -> Boolean = { lambdaError() },
) : AccountProviderAccessControl {
    override suspend fun isAllowedToConnectToAccountProvider(accountProviderUrl: String): Boolean {
        return isAllowedToConnectToAccountProviderResult(accountProviderUrl)
    }
}
