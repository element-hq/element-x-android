/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl

import org.matrix.rustcomponents.sdk.ClientSessionDelegate
import org.matrix.rustcomponents.sdk.Session

/**
 * A [ClientSessionDelegate] which does nothing, to be used for the clients which are not associated to a stored session,
 * for instance the temporary clients, or the clients used to perform a login. The session is only stored once the login
 * is done, with a client created by [RustMatrixClientFactory.create], which uses a [RustClientSessionDelegate].
 */
internal object NoOpClientSessionDelegate : ClientSessionDelegate {
    override fun saveSessionInKeychain(session: Session) = Unit

    override fun retrieveSessionFromKeychain(userId: String): Session {
        // This should never be called, as it's only used for multi-process setups
        error("retrieveSessionFromKeychain should never be called for Android")
    }
}
