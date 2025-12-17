/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.fixtures

import io.element.android.features.lockscreen.impl.pin.DefaultPinCodeManager
import io.element.android.features.lockscreen.impl.pin.PinCodeManager
import io.element.android.features.lockscreen.impl.pin.storage.InMemoryLockScreenStore
import io.element.android.features.lockscreen.impl.storage.LockScreenStore
import io.element.android.libraries.cryptography.api.EncryptionDecryptionService
import io.element.android.libraries.cryptography.impl.AESEncryptionDecryptionService
import io.element.android.libraries.cryptography.test.SimpleSecretKeyRepository

internal fun aPinCodeManager(
    lockScreenStore: LockScreenStore = InMemoryLockScreenStore(),
    secretKeyRepository: SimpleSecretKeyRepository = SimpleSecretKeyRepository(),
    encryptionDecryptionService: EncryptionDecryptionService = AESEncryptionDecryptionService(),
): PinCodeManager {
    return DefaultPinCodeManager(secretKeyRepository, encryptionDecryptionService, lockScreenStore)
}
