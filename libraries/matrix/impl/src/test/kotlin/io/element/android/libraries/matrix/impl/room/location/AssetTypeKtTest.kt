/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.location.AssetType
import org.junit.Test

class AssetTypeKtTest {
    @Test
    fun toInner() {
        assertThat(AssetType.SENDER.toInner()).isEqualTo(org.matrix.rustcomponents.sdk.AssetType.SENDER)
        assertThat(AssetType.PIN.toInner()).isEqualTo(org.matrix.rustcomponents.sdk.AssetType.PIN)
    }
}
