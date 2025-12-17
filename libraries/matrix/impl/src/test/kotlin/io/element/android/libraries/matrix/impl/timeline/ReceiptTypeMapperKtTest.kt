/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */
package io.element.android.libraries.matrix.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.ReceiptType
import org.junit.Test
import org.matrix.rustcomponents.sdk.ReceiptType as RustReceiptType

class ReceiptTypeMapperKtTest {
    @Test
    fun toRustReceiptType() {
        assertThat(ReceiptType.READ.toRustReceiptType()).isEqualTo(RustReceiptType.READ)
        assertThat(ReceiptType.READ_PRIVATE.toRustReceiptType()).isEqualTo(RustReceiptType.READ_PRIVATE)
        assertThat(ReceiptType.FULLY_READ.toRustReceiptType()).isEqualTo(RustReceiptType.FULLY_READ)
    }
}
