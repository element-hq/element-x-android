/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import io.element.android.libraries.matrix.impl.keys.PassphraseGenerator
import io.element.android.libraries.matrix.test.A_PASSPHRASE

class FakePassphraseGenerator(
    private val passphrase: () -> String? = { A_PASSPHRASE }
) : PassphraseGenerator {
    override fun generatePassphrase(): String? = passphrase()
}
