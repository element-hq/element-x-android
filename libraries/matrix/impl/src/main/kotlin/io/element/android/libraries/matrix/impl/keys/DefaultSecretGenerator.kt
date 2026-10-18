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
import io.element.android.libraries.androidutils.crypto.ClientSecret
import java.security.SecureRandom

@ContributesBinding(AppScope::class)
class DefaultSecretGenerator : SecretGenerator {
    override fun generatePassphrase(size: Int): ClientSecret.Passphrase? {
        val key = ByteArray(size = size)
        SecureRandom().nextBytes(key)
        return ClientSecret.Passphrase(Base64.encodeToString(key, Base64.NO_PADDING or Base64.NO_WRAP))
    }

    override fun generateKey(size: Int): ClientSecret.RawKey {
        val key = ByteArray(size = size)
        SecureRandom().nextBytes(key)
        return ClientSecret.RawKey(key)
    }
}
