/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test

import io.element.android.libraries.matrix.api.HomeserverCapabilitiesProvider

class FakeHomeserverCapabilitiesProvider(
    private val refresh: () -> Result<Unit> = { Result.success(Unit) },
    private val canChangeDisplayName: () -> Result<Boolean> = { Result.success(true) },
    private val canChangeAvatarUrl: () -> Result<Boolean> = { Result.success(true) },
) : HomeserverCapabilitiesProvider {
    override suspend fun refresh(): Result<Unit> = refresh.invoke()
    override suspend fun canChangeDisplayName(): Result<Boolean> = canChangeDisplayName.invoke()
    override suspend fun canChangeAvatarUrl(): Result<Boolean> = canChangeAvatarUrl.invoke()
}
