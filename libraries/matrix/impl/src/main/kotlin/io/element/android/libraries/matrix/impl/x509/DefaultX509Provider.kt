/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import android.app.Activity
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.SingleIn
import io.element.android.libraries.matrix.api.x509.RawX509Signer
import io.element.android.libraries.matrix.api.x509.RawX509Verifier
import io.element.android.libraries.matrix.api.x509.X509Provider

/**
 * Default implementation of [X509Provider], which does nothing.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultX509Provider : X509Provider {
    override suspend fun onAppStartup(parentActivity: Activity) {
        // Nothing to do here.
    }

    override suspend fun getRawX509Signer(): RawX509Signer? {
        return null
    }

    override suspend fun getRawX509Verifier(): RawX509Verifier? {
        return null
    }
}
