/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.draft

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VolatileComposerDraftStoreTest {
    private val roomId = A_ROOM_ID
    private val sut = VolatileComposerDraftStore()
    private val draft = ComposerDraft("plainText", "htmlText", ComposerDraftType.NewMessage)

    @Test
    fun `when storing a non-null draft and then loading it, it's loaded and removed`() = runTest {
        val initialDraft = sut.loadDraft(roomId)
        assertThat(initialDraft).isNull()

        sut.updateDraft(roomId, draft)

        val loadedDraft = sut.loadDraft(roomId)
        assertThat(loadedDraft).isEqualTo(draft)

        val loadedDraftAfter = sut.loadDraft(roomId)
        assertThat(loadedDraftAfter).isNull()
    }

    @Test
    fun `when storing a null draft and then loading it, it's removing the previous one`() = runTest {
        val initialDraft = sut.loadDraft(roomId)
        assertThat(initialDraft).isNull()

        sut.updateDraft(roomId, draft)
        sut.updateDraft(roomId, null)

        val loadedDraft = sut.loadDraft(roomId)
        assertThat(loadedDraft).isNull()
    }
}
