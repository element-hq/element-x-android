/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserIdTest {
    @Test
    fun `valid user id`() {
        val userId = UserId("@alice:example.org")
        assertThat(userId.extractedDisplayName).isEqualTo("alice")
        assertThat(userId.domainName).isEqualTo("example.org")
    }
}
