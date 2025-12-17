/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.root

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.test.AN_EXCEPTION
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_ROOM_ID_2
import io.element.android.libraries.matrix.test.A_ROOM_ID_3
import org.junit.Test

class SpaceStateTest {
    @Test
    fun `test default state`() {
        val state = aSpaceState()
        assertThat(state.hasAnyFailure).isFalse()
        assertThat(state.isJoining(A_ROOM_ID)).isFalse()
    }

    @Test
    fun `test has failure`() {
        val state = aSpaceState(
            joinActions = mapOf(
                A_ROOM_ID to AsyncAction.Uninitialized,
                A_ROOM_ID_2 to AsyncAction.Failure(AN_EXCEPTION),
                A_ROOM_ID_3 to AsyncAction.Success(Unit),
            )
        )
        assertThat(state.hasAnyFailure).isTrue()
    }

    @Test
    fun `test isJoining`() {
        val state = aSpaceState(
            joinActions = mapOf(
                A_ROOM_ID to AsyncAction.Loading,
            )
        )
        assertThat(state.isJoining(A_ROOM_ID)).isTrue()
    }
}
