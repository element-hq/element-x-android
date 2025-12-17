/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import androidx.compose.runtime.saveable.SaverScope
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class PollFormStateSaverTest {
    companion object {
        val CanSaveScope = SaverScope { true }
    }

    @Test
    fun `test save and restore`() {
        val state = PollFormState(
            question = "question",
            answers = persistentListOf("answer1", "answer2"),
            isDisclosed = true,
        )

        val saved = with(CanSaveScope) {
            with(pollFormStateSaver) {
                save(state)
            }
        }

        val restored = saved?.let {
            pollFormStateSaver.restore(it)
        }

        assertThat(restored).isEqualTo(state)
    }
}
