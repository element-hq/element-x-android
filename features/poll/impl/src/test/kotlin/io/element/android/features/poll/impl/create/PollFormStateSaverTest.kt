/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.poll.impl.create

import androidx.compose.runtime.saveable.SaverScope
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.toPersistentList
import org.junit.Test

class PollFormStateSaverTest {
    companion object {
        val CanSaveScope = SaverScope { true }
    }

    @Test
    fun `test save and restore`() {
        val state = PollFormState(
            question = "question",
            answers = listOf("answer1", "answer2").toPersistentList(),
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
