/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.draft

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft

interface ComposerDraftStore {
    suspend fun loadDraft(roomId: RoomId): ComposerDraft?
    suspend fun updateDraft(roomId: RoomId, draft: ComposerDraft?)
}
