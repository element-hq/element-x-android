/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test

import io.element.android.libraries.push.api.GetCurrentPushProvider

class FakeGetCurrentPushProvider(
    private val currentPushProvider: String?
) : GetCurrentPushProvider {
    override suspend fun getCurrentPushProvider(): String? = currentPushProvider
}
