/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultPassphraseGeneratorTest {
    @Test
    fun `check that generated passphrase has the expected length`() {
        val passphraseGenerator = DefaultPassphraseGenerator()
        val passphrase = passphraseGenerator.generatePassphrase()
        assertThat(passphrase!!.length).isEqualTo(342)
    }
}
