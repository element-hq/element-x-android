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

@ContributesBinding(AppScope::class)
@Inject
class RustHomeServerLoginCompatibilityChecker(
    private val clientBuilderProvider: ClientBuilderProvider,
) : HomeServerLoginCompatibilityChecker {
    override suspend fun check(url: String): Result<Boolean> = runCatchingExceptions {
        val client = clientBuilderProvider.provide().homeserverUrl(url).build()
        client.use {
            it.homeserverLoginDetails()
        }.use {
            it.supportsOidcLogin() || it.supportsPasswordLogin()
        }
    }
}
