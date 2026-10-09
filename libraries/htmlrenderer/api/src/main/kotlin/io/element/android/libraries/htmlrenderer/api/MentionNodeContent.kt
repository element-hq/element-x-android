/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import androidx.compose.runtime.Immutable
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId

/**
 * Describes a mention "pill" referenced from a [ParagraphNode].
 *
 * Each mention occupies an inline-content placeholder inside the paragraph's
 * [ParagraphNode.text]; the renderer looks the id up in [ParagraphNode.inlineContent]
 * and draws the corresponding pill.
 */
@Immutable
sealed interface MentionNodeContent {
    /** The text shown inside the pill (e.g. a display name, prefixed with `@` for users). */
    val displayText: String

    /** A mention of a specific user (`<a href="matrix.to/#/@user:server">`). */
    data class User(
        override val displayText: String,
        val userId: UserId,
        val permalinkUrl: String,
    ) : MentionNodeContent

    /** A mention of a room (`<a href="matrix.to/#/#room:server">`). */
    data class Room(
        override val displayText: String,
        val roomIdOrAlias: RoomIdOrAlias,
        val permalinkUrl: String,
    ) : MentionNodeContent

    /** An `@room` mention notifying everyone in the room. */
    data class Everyone(
        override val displayText: String,
    ) : MentionNodeContent
}
