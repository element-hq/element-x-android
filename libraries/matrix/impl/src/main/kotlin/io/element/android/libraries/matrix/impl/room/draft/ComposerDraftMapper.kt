/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.draft

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import org.matrix.rustcomponents.sdk.ComposerDraft as RustComposerDraft
import org.matrix.rustcomponents.sdk.ComposerDraftType as RustComposerDraftType

internal fun ComposerDraft.into(): RustComposerDraft {
    return RustComposerDraft(
        plainText = plainText,
        htmlText = htmlText,
        draftType = draftType.into(),
        // TODO add media attachments to the draft
        attachments = emptyList(),
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
