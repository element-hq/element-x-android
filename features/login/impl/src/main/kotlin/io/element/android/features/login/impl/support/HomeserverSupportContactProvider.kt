/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.support

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.text.takeIfNotBlank
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.core.uri.ensureProtocol
import io.element.android.libraries.network.RetrofitFactory
import timber.log.Timber

private const val ADMIN_ROLE = "m.role.admin"

/**
 * Reads the administrator contact a homeserver advertises at
 * [GET /.well-known/matrix/support](https://spec.matrix.org/v1.16/client-server-api/#getwell-knownmatrixsupport).
 */
interface HomeserverSupportContactProvider {
    /**
     * @return something the user can use to reach the homeserver's administrator — an email address, a Matrix ID or a
     * support page — or null when the homeserver advertises none or cannot be reached.
     */
    suspend fun getContact(homeserverUrl: String): String?
}

@ContributesBinding(AppScope::class)
class DefaultHomeserverSupportContactProvider(
    private val retrofitFactory: RetrofitFactory,
) : HomeserverSupportContactProvider {
    override suspend fun getContact(homeserverUrl: String): String? {
        return runCatchingExceptions {
            retrofitFactory.create(homeserverUrl.ensureProtocol())
                .create(MatrixSupportApi::class.java)
                .getSupport()
                .preferredContact()
        }
            .onFailure { Timber.d(it, "No support contact advertised by the homeserver") }
            .getOrNull()
    }
}

internal fun MatrixSupport.preferredContact(): String? {
    val admins = contacts.orEmpty().filter { it.role == ADMIN_ROLE }
    val candidates = admins.ifEmpty { contacts.orEmpty() }
    return candidates.firstNotNullOfOrNull { it.emailAddress?.takeIfNotBlank() ?: it.matrixId?.takeIfNotBlank() }
        ?: supportPage?.takeIfNotBlank()
}
