/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.cryptography.api

import android.security.keystore.KeyProperties

object AESEncryptionSpecs {
    const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    const val PADDINGS = KeyProperties.ENCRYPTION_PADDING_NONE
    const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    const val KEY_SIZE = 128
    const val CIPHER_TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDINGS"
}
