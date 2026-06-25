/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.api

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CustomRecoveryPassphraseTest {
    @Test
    fun `empty input never satisfies a positive minimum`() {
        assertThat(CustomRecoveryPassphrase(minCharacterCount = 1).isSatisfiedBy("")).isFalse()
    }

    @Test
    fun `input shorter than minimum is not satisfied`() {
        assertThat(CustomRecoveryPassphrase(minCharacterCount = 8).isSatisfiedBy("abc")).isFalse()
    }

    @Test
    fun `input exactly at minimum is satisfied`() {
        assertThat(CustomRecoveryPassphrase(minCharacterCount = 8).isSatisfiedBy("abcdefgh")).isTrue()
    }

    @Test
    fun `input longer than minimum is satisfied`() {
        assertThat(CustomRecoveryPassphrase(minCharacterCount = 4).isSatisfiedBy("abcdefgh")).isTrue()
    }
}
