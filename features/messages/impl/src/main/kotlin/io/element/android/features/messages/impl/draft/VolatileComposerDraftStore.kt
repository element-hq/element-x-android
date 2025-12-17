/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.draft

import dev.zacsweers.metro.Inject
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.ThreadId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft

/**
 * A volatile draft store that keeps drafts in memory only.
 * It can be used to store drafts that should not be persisted across app restarts.
 * Currently it's used to store draft message when moving to edit mode.
 */
@Inject
class VolatileComposerDraftStore : ComposerDraftStore {
    private val drafts: MutableMap<String, ComposerDraft> = mutableMapOf()

    override suspend fun loadDraft(roomId: RoomId, threadRoot: ThreadId?): ComposerDraft? {
        val key = threadRoot?.value ?: roomId.value
        // Remove the draft from the map when it is loaded
        return drafts.remove(key)
    }

    override suspend fun updateDraft(roomId: RoomId, threadRoot: ThreadId?, draft: ComposerDraft?) {
        val key = threadRoot?.value ?: roomId.value
        if (draft == null) {
            drafts.remove(key)
        } else {
            drafts[key] = draft
        }
    }
}
