/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.draft

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_THREAD_ID
import kotlinx.coroutines.test.runTest
import org.junit.Test

class VolatileComposerDraftStoreTest {
    private val roomId = A_ROOM_ID
    private val sut = VolatileComposerDraftStore()
    private val draft = ComposerDraft("plainText", "htmlText", ComposerDraftType.NewMessage)

    @Test
    fun `when storing a non-null draft and then loading it, it's loaded and removed`() = runTest {
        val initialDraft = sut.loadDraft(roomId = roomId, threadRoot = null)
        assertThat(initialDraft).isNull()

        sut.updateDraft(roomId = roomId, threadRoot = null, draft = draft)

        val loadedDraft = sut.loadDraft(roomId = roomId, threadRoot = null)
        assertThat(loadedDraft).isEqualTo(draft)

        val loadedDraftAfter = sut.loadDraft(roomId = roomId, threadRoot = null)
        assertThat(loadedDraftAfter).isNull()

        // In thread:
        val threadRoot = A_THREAD_ID
        val initialThreadDraft = sut.loadDraft(roomId = roomId, threadRoot = threadRoot)
        assertThat(initialThreadDraft).isNull()

        sut.updateDraft(roomId = roomId, threadRoot = threadRoot, draft = draft)

        val loadedThreadDraft = sut.loadDraft(roomId = roomId, threadRoot = threadRoot)
        assertThat(loadedThreadDraft).isEqualTo(draft)

        val loadedThreadDraftAfter = sut.loadDraft(roomId = roomId, threadRoot = threadRoot)
        assertThat(loadedThreadDraftAfter).isNull()
    }

    @Test
    fun `when storing a null draft and then loading it, it's removing the previous one`() = runTest {
        val initialDraft = sut.loadDraft(roomId = roomId, threadRoot = null)
        assertThat(initialDraft).isNull()

        sut.updateDraft(roomId = roomId, threadRoot = null, draft = draft)
        sut.updateDraft(roomId = roomId, threadRoot = null, draft = null)

        val loadedDraft = sut.loadDraft(roomId = roomId, threadRoot = null)
        assertThat(loadedDraft).isNull()

        // In thread:
        val threadRoot = A_THREAD_ID
        val initialThreadDraft = sut.loadDraft(roomId = roomId, threadRoot = threadRoot)
        assertThat(initialThreadDraft).isNull()

        sut.updateDraft(roomId = roomId, threadRoot = threadRoot, draft = draft)
        sut.updateDraft(roomId = roomId, threadRoot = threadRoot, draft = null)

        val loadedThreadDraft = sut.loadDraft(roomId = roomId, threadRoot = threadRoot)
        assertThat(loadedThreadDraft).isNull()
    }
}
