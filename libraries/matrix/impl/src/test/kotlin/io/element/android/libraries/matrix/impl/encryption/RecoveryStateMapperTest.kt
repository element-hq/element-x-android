/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.encryption

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.encryption.RecoveryState
import org.junit.Test
import org.matrix.rustcomponents.sdk.RecoveryState as RustRecoveryState

class RecoveryStateMapperTest {
    @Test
    fun `Ensure that mapping is correct`() {
        val sut = RecoveryStateMapper()
        assertThat(sut.map(RustRecoveryState.UNKNOWN)).isEqualTo(RecoveryState.UNKNOWN)
        assertThat(sut.map(RustRecoveryState.ENABLED)).isEqualTo(RecoveryState.ENABLED)
        assertThat(sut.map(RustRecoveryState.DISABLED)).isEqualTo(RecoveryState.DISABLED)
        assertThat(sut.map(RustRecoveryState.INCOMPLETE)).isEqualTo(RecoveryState.INCOMPLETE)
    }
}
