/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.draft

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultComposerDraftService @Inject constructor(
    private val volatileComposerDraftStore: VolatileComposerDraftStore,
    private val matrixComposerDraftStore: MatrixComposerDraftStore,
) : ComposerDraftService {
    override suspend fun loadDraft(roomId: RoomId, isVolatile: Boolean): ComposerDraft? {
        return getStore(isVolatile).loadDraft(roomId)
    }

    override suspend fun updateDraft(roomId: RoomId, draft: ComposerDraft?, isVolatile: Boolean) {
        getStore(isVolatile).updateDraft(roomId, draft)
    }

    private fun getStore(isVolatile: Boolean): ComposerDraftStore {
        return if (isVolatile) {
            volatileComposerDraftStore
        } else {
            matrixComposerDraftStore
        }
    }
}
