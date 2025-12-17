/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

import android.util.Base64
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import java.security.SecureRandom

private const val SECRET_SIZE = 256

@ContributesBinding(AppScope::class)
class DefaultPassphraseGenerator : PassphraseGenerator {
    override fun generatePassphrase(): String? {
        val key = ByteArray(size = SECRET_SIZE)
        SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
