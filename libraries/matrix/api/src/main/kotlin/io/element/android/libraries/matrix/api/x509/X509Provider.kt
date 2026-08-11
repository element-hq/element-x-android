/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.x509

import android.app.Activity

/**
 * Interface for things which can provide a [RawX509Signer] and/or a [RawX509Verifier]. Called during client creation.
 */
interface X509Provider {
    /** Hook which is called during app startup. Allows the implementation to do any necessary initialization. */
    suspend fun onAppStartup(parentActivity: Activity)

    /**
     * Provide a [RawX509Signer].
     *
     * If a non-null result is returned, the implementation will be called to add a signature to any newly-created
     * digital identity (i.e., master cross-signing key).
     */
    suspend fun getRawX509Signer(): RawX509Signer?

    /**
     * Provide a [RawX509Verifier].
     *
     * If a non-null result is returned, the implementation will be called to verify X.509 signatures on users'
     * digital identities (i.e. master cross-signing keys).
     */
    suspend fun getRawX509Verifier(): RawX509Verifier?
}
