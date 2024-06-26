/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.draft

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import timber.log.Timber
import javax.inject.Inject

@ContributesBinding(RoomScope::class)
class DefaultComposerDraftService @Inject constructor(
    private val client: MatrixClient,
) : ComposerDraftService {
    override suspend fun loadDraft(roomId: RoomId): ComposerDraft? {
        return client.getRoom(roomId)?.use { room ->
            room.loadComposerDraft()
                .onFailure {
                    Timber.e(it, "Failed to load composer draft for room $roomId")
                }
                .onSuccess { draft ->
                    room.clearComposerDraft()
                    Timber.d("Loaded composer draft for room $roomId : $draft")
                }.getOrNull()
        }
    }

    override suspend fun saveDraft(roomId: RoomId, draft: ComposerDraft) {
        client.getRoom(roomId)?.use { room ->
            room.saveComposerDraft(draft)
                .onFailure {
                    Timber.e(it, "Failed to save composer draft for room $roomId")
                }
                .onSuccess {
                    Timber.d("Saved composer draft for room $roomId")
                }
        }
    }
}
