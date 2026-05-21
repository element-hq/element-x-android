/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.impl.ClientSecret
import io.element.android.libraries.matrix.impl.keys.SecretGenerator
import io.element.android.libraries.matrix.test.A_PASSPHRASE

class FakeSecretGenerator(
    private val passphrase: (Int) -> String? = { A_PASSPHRASE },
    private val key: (Int) -> ByteArray = { ByteArray(it) { 0 } },
) : SecretGenerator {
    override fun generatePassphrase(size: Int): ClientSecret.Passphrase? = passphrase(size)?.let { ClientSecret.Passphrase(it) }
    override fun generateKey(size: Int): ClientSecret.RawKey = ClientSecret.RawKey(key(size))
}
