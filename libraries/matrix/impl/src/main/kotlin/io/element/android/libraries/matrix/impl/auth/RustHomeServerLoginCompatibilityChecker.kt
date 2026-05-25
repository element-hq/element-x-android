/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.auth.HomeServerLoginCompatibilityChecker
import io.element.android.libraries.matrix.impl.ClientBuilderProvider
import io.element.android.libraries.matrix.impl.certificates.UserCertificatesProvider
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.flow.first
import timber.log.Timber

@ContributesBinding(AppScope::class)
class RustHomeServerLoginCompatibilityChecker(
    private val clientBuilderProvider: ClientBuilderProvider,
    private val userCertificatesProvider: UserCertificatesProvider,
    private val appPreferencesStore: AppPreferencesStore,
    ) : HomeServerLoginCompatibilityChecker {
    override suspend fun check(url: String): Result<Boolean> = runCatchingExceptions {
        clientBuilderProvider.provide()
            .inMemoryStore()
            .serverNameOrHomeserverUrl(url)
            .run {
                if (appPreferencesStore.getUseCustomCertificatesFlow().first() == true) {
                    addRootCertificates(userCertificatesProvider.provides())
                } else {
                    this
                }
            }
            .build()
            .use {
                it.homeserverLoginDetails()
            }
            .use {
                Timber.d("Homeserver $url | OAuth: ${it.supportsOauthLogin()} | Password: ${it.supportsPasswordLogin()} | SSO: ${it.supportsSsoLogin()}")
                it.supportsOauthLogin() || it.supportsPasswordLogin()
            }
    }
}
