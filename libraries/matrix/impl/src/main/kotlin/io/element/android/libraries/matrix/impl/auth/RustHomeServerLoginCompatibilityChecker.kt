/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.auth.HomeServerLoginCompatibilityChecker
import io.element.android.libraries.matrix.impl.ClientBuilderProvider
import timber.log.Timber

@ContributesBinding(AppScope::class)
@Inject
class RustHomeServerLoginCompatibilityChecker(
    private val clientBuilderProvider: ClientBuilderProvider,
) : HomeServerLoginCompatibilityChecker {
    override suspend fun check(url: String): Result<Boolean> = runCatchingExceptions {
        clientBuilderProvider.provide()
            .inMemoryStore()
            .serverNameOrHomeserverUrl(url)
            .build()
            .use {
                it.homeserverLoginDetails()
            }
            .use {
                Timber.d("Homeserver $url | OIDC: ${it.supportsOidcLogin()} | Password: ${it.supportsPasswordLogin()} | SSO: ${it.supportsSsoLogin()}")
                it.supportsOidcLogin() || it.supportsPasswordLogin()
            }
    }
}
