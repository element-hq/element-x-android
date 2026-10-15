/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NoopRoomCallActionPolicyTest {
    @Test
    fun `default policy permits the call action`() = runTest {
        assertThat(NoopRoomCallActionPolicy().isCallActionEnabledFlow().first()).isTrue()
    }
}
