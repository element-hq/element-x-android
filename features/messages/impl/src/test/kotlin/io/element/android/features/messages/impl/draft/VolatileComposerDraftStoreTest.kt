/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
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
