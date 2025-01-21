/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.test

import io.element.android.features.share.api.ShareService
import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.CoroutineScope

class FakeShareService(
    private val observeFeatureFlagLambda: (CoroutineScope) -> Unit = { lambdaError() }
) : ShareService {
    override fun observeFeatureFlag(coroutineScope: CoroutineScope) {
        observeFeatureFlagLambda(coroutineScope)
    }
}
