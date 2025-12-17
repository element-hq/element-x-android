/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.colors

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AvatarColorsTest {
    private val maxSize = 6
    @Test
    fun `compute string hash`() {
        assertThat("@alice:domain.org".toHash(maxSize)).isEqualTo(0)
        assertThat("@bob:domain.org".toHash(maxSize)).isEqualTo(1)
        assertThat("@charlie:domain.org".toHash(maxSize)).isEqualTo(2)
    }

    @Test
    fun `compute string hash reverse`() {
        assertThat("0".toHash(maxSize)).isEqualTo(0)
        assertThat("1".toHash(maxSize)).isEqualTo(1)
        assertThat("2".toHash(maxSize)).isEqualTo(2)
        assertThat("3".toHash(maxSize)).isEqualTo(3)
        assertThat("4".toHash(maxSize)).isEqualTo(4)
        assertThat("5".toHash(maxSize)).isEqualTo(5)
        assertThat("6".toHash(maxSize)).isEqualTo(0)
        assertThat("7".toHash(maxSize)).isEqualTo(1)
    }
}
