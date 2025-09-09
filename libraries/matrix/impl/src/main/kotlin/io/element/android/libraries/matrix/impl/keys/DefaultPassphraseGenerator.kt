/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

import android.util.Base64
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.security.SecureRandom

private const val SECRET_SIZE = 256

@ContributesBinding(AppScope::class)
@Inject class DefaultPassphraseGenerator : PassphraseGenerator {
    override fun generatePassphrase(): String? {
        val key = ByteArray(size = SECRET_SIZE)
        SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
