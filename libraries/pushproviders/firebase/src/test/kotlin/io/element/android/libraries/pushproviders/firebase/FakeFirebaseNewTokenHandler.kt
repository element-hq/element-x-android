/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import io.element.android.tests.testutils.lambda.lambdaError

class FakeFirebaseNewTokenHandler(
    private val handleResult: (String) -> Unit = { lambdaError() }
) : FirebaseNewTokenHandler {
    override suspend fun handle(firebaseToken: String) {
        handleResult(firebaseToken)
    }
}
