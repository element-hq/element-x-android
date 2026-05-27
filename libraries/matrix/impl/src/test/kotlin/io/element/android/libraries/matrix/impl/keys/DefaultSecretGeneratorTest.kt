/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.keys

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.androidutils.crypto.ClientSecret
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultSecretGeneratorTest {
    @Test
    fun `check that generated passphrase has the expected length`() {
        val secretGenerator = DefaultSecretGenerator()
        val passphrase = secretGenerator.generatePassphrase(256)
        assertThat(passphrase).isInstanceOf(ClientSecret.Passphrase::class.java)
        // Size after Base64 encoding should be 4/3 of the original size, without padding
        assertThat(passphrase!!.value).hasLength(342)
    }

    @Test
    fun `check that generated key has the expected length`() {
        val secretGenerator = DefaultSecretGenerator()
        val key = secretGenerator.generateKey(123)
        assertThat(key).isInstanceOf(ClientSecret.RawKey::class.java)
        assertThat(key.bytes).hasLength(123)
    }
}
