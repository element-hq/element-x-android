/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import io.element.android.libraries.pushproviders.api.CurrentUserPushConfig
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushCurrentUserPushConfigProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushCurrentUserPushConfigProvider(
    private val currentUserPushConfig: () -> CurrentUserPushConfig? = { lambdaError() },
) : UnifiedPushCurrentUserPushConfigProvider {
    override suspend fun provide(): CurrentUserPushConfig? {
        return currentUserPushConfig()
    }
}
