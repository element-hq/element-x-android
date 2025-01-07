/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.draft

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import javax.inject.Inject

/**
 * A volatile draft store that keeps drafts in memory only.
 * It can be used to store drafts that should not be persisted across app restarts.
 * Currently it's used to store draft message when moving to edit mode.
 */
class VolatileComposerDraftStore @Inject constructor() : ComposerDraftStore {
    private val drafts: MutableMap<RoomId, ComposerDraft> = mutableMapOf()

    override suspend fun loadDraft(roomId: RoomId): ComposerDraft? {
        // Remove the draft from the map when it is loaded
        return drafts.remove(roomId)
    }

    override suspend fun updateDraft(roomId: RoomId, draft: ComposerDraft?) {
        if (draft == null) {
            drafts.remove(roomId)
        } else {
            drafts[roomId] = draft
        }
    }
}
