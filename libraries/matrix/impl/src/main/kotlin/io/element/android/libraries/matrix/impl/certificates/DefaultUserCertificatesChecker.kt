/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.certificates

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.certificates.UserCertificatesChecker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ContributesBinding(AppScope::class)
class DefaultUserCertificatesChecker(
    private val userCertificatesProvider: UserCertificatesProvider,
) : UserCertificatesChecker {
    override suspend fun hasCertificates(): Boolean {
        return withContext(Dispatchers.IO) {
            userCertificatesProvider.hasCertificates()
        }
    }
}
