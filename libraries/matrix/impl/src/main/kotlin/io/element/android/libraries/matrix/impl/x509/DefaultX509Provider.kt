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
import io.element.android.libraries.matrix.api.x509.X509Provider
import io.element.android.libraries.matrix.api.x509.X509Sign
import io.element.android.libraries.matrix.api.x509.X509Verify

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class DefaultX509Provider: X509Provider {
    override suspend fun initKeyAlias(parentActivity: Activity) {
    }

    override suspend fun getX509Sign(): X509Sign? {
        return null
    }

    override suspend fun getX509Verify(): X509Verify? {
        return null
    }
}
