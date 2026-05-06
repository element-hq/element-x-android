/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.x509

import android.app.Activity

interface X509Provider {
    suspend fun initKeyAlias(parentActivity: Activity)

    suspend fun getX509KeyPair(): X509KeyPair?
    suspend fun getX509TustRoot(): X509TrustRoot?
}

