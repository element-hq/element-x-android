/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.logout.api.LogoutUseCase
import dev.zacsweers.metro.AppScope
import io.element.android.libraries.matrix.api.MatrixClientProvider
import io.element.android.libraries.matrix.api.auth.MatrixAuthenticationService
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class DefaultLogoutUseCase(
    private val authenticationService: MatrixAuthenticationService,
    private val matrixClientProvider: MatrixClientProvider,
) : LogoutUseCase {
    override suspend fun logout(ignoreSdkError: Boolean) {
        val currentSession = authenticationService.getLatestSessionId()
        if (currentSession != null) {
            matrixClientProvider.getOrRestore(currentSession)
                .getOrThrow()
                .logout(userInitiated = true, ignoreSdkError = true)
        } else {
            error("No session to sign out")
        }
    }
}
