/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.tools

import javax.inject.Inject

private const val RECOVERY_KEY_LENGTH = 48
private const val BASE_58_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"

class RecoveryKeyTools @Inject constructor() {
    fun isRecoveryKeyFormatValid(recoveryKey: String): Boolean {
        val recoveryKeyWithoutSpace = recoveryKey.replace("\\s+".toRegex(), "")
        return recoveryKeyWithoutSpace.length == RECOVERY_KEY_LENGTH && recoveryKeyWithoutSpace.all { BASE_58_ALPHABET.contains(it) }
    }
}
