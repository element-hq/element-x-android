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

package io.element.android.libraries.matrix.impl.room.draft

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import uniffi.matrix_sdk_base.ComposerDraft as RustComposerDraft
import uniffi.matrix_sdk_base.ComposerDraftType as RustComposerDraftType

internal fun ComposerDraft.into(): RustComposerDraft {
    return RustComposerDraft(
        plainText = plainText,
        htmlText = htmlText,
        draftType = draftType.into()
    )
}

internal fun RustComposerDraft.into(): ComposerDraft {
    return ComposerDraft(
        plainText = plainText,
        htmlText = htmlText,
        draftType = draftType.into()
    )
}

private fun RustComposerDraftType.into(): ComposerDraftType {
    return when (this) {
        RustComposerDraftType.NewMessage -> ComposerDraftType.NewMessage
        is RustComposerDraftType.Reply -> ComposerDraftType.Reply(EventId(eventId))
        is RustComposerDraftType.Edit -> ComposerDraftType.Edit(EventId(eventId))
    }
}

private fun ComposerDraftType.into(): RustComposerDraftType {
    return when (this) {
        ComposerDraftType.NewMessage -> RustComposerDraftType.NewMessage
        is ComposerDraftType.Reply -> RustComposerDraftType.Reply(eventId.value)
        is ComposerDraftType.Edit -> RustComposerDraftType.Edit(eventId.value)
    }
}
