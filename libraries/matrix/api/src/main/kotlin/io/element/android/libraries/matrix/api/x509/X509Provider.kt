/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.x509

import android.app.Activity

/**
 * Interface for things which can provide a [RawX509Signer] and/or a [RawX509Verifier].
 */
interface X509Provider {
    /**
     * Hook which is called during app startup. Allows the implementation to do any necessary initialization.
     *
     * Note that the hook is run in the background: it will not block startup of the app.
     *
     * @param activity The activity which is active while the app starts (i.e. the `MainActivity`).
     * */
    suspend fun onAppStartup(activity: Activity)

    /**
     * Provide a [RawX509Signer]. Called during client creation.
     *
     * If a non-null result is returned, the implementation will be called to add a signature to any newly-created
     * digital identity (i.e., master cross-signing key).
     */
    suspend fun getRawX509Signer(): RawX509Signer?

    /**
     * Provide a [RawX509Verifier]. Called during client creation.
     *
     * If a non-null result is returned, the implementation will be called to verify X.509 signatures on users'
     * digital identities (i.e. master cross-signing keys).
     */
    suspend fun getRawX509Verifier(): RawX509Verifier?
}
