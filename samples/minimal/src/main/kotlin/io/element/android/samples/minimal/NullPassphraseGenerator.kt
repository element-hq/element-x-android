/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.samples.minimal

import io.element.android.libraries.matrix.impl.keys.PassphraseGenerator

class NullPassphraseGenerator : PassphraseGenerator {
    override fun generatePassphrase(): String? = null
}
