/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

import android.util.Base64
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import java.security.SecureRandom
import javax.inject.Inject

private const val SECRET_SIZE = 256

@ContributesBinding(AppScope::class)
class DefaultPassphraseGenerator @Inject constructor() : PassphraseGenerator {
    override fun generatePassphrase(): String? {
        val key = ByteArray(size = SECRET_SIZE)
        SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
