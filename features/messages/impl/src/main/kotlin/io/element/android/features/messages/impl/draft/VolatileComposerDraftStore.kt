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

import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import javax.inject.Inject

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
