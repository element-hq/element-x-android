/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.draft

import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import timber.log.Timber
import javax.inject.Inject

/**
 * A draft store that persists drafts in the room state.
 * It can be used to store drafts that should be persisted across app restarts.
 */
class MatrixComposerDraftStore @Inject constructor(
    private val client: MatrixClient,
) : ComposerDraftStore {
    override suspend fun loadDraft(roomId: RoomId): ComposerDraft? {
        return client.getRoom(roomId)?.use { room ->
            room.loadComposerDraft()
                .onFailure {
                    Timber.e(it, "Failed to load composer draft for room $roomId")
                }
                .onSuccess { draft ->
                    room.clearComposerDraft()
                    Timber.d("Loaded composer draft for room $roomId : $draft")
                }
                .getOrNull()
        }
    }

    override suspend fun updateDraft(roomId: RoomId, draft: ComposerDraft?) {
        client.getRoom(roomId)?.use { room ->
            val updateDraftResult = if (draft == null) {
                room.clearComposerDraft()
            } else {
                room.saveComposerDraft(draft)
            }
            updateDraftResult
                .onFailure {
                    Timber.e(it, "Failed to update composer draft for room $roomId")
                }
                .onSuccess {
                    Timber.d("Updated composer draft for room $roomId")
                }
        }
    }
}
