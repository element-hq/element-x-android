/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.RecoveryException
import org.junit.Test
import org.matrix.rustcomponents.sdk.RecoveryException as RustRecoveryException

class RecoveryExceptionMapperTest {
    @Test
    fun `Import exception is mapped to the application boundary`() {
        val result = RustRecoveryException.Import("Failed to import room keys").mapRecoveryException()

        assertThat(result).isInstanceOf(RecoveryException.Import::class.java)
        assertThat(result.message).isEqualTo("Failed to import room keys")
    }
}
