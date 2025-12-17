/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.room.draft

import io.element.android.libraries.matrix.api.core.EventId

sealed interface ComposerDraftType {
    data object NewMessage : ComposerDraftType
    data class Reply(val eventId: EventId) : ComposerDraftType
    data class Edit(val eventId: EventId) : ComposerDraftType
}
