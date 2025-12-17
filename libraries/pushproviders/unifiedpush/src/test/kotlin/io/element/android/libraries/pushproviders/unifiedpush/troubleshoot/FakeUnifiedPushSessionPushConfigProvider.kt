/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.pushproviders.api.Config
import io.element.android.libraries.pushproviders.unifiedpush.UnifiedPushSessionPushConfigProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakeUnifiedPushSessionPushConfigProvider(
    private val config: (SessionId) -> Config? = { lambdaError() },
) : UnifiedPushSessionPushConfigProvider {
    override suspend fun provide(sessionId: SessionId): Config? {
        return config(sessionId)
    }
}
