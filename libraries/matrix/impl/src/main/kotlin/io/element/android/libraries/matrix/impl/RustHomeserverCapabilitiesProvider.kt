/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.HomeserverCapabilitiesProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.matrix.rustcomponents.sdk.HomeserverCapabilities

class RustHomeserverCapabilitiesProvider(
    private val homeserverCapabilities: HomeserverCapabilities,
    private val dispatcher: CoroutineDispatcher,
) : HomeserverCapabilitiesProvider {
    override suspend fun refresh(): Result<Unit> = withContext(dispatcher) {
        runCatchingExceptions {
            homeserverCapabilities.refresh()
        }
    }

    override suspend fun canChangeDisplayName(): Result<Boolean> = withContext(dispatcher) {
        runCatchingExceptions {
            homeserverCapabilities.canChangeDisplayname()
        }
    }

    override suspend fun canChangeAvatarUrl(): Result<Boolean> = withContext(dispatcher) {
        runCatchingExceptions {
            homeserverCapabilities.canChangeAvatar()
        }
    }
}
