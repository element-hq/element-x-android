/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.htmlrenderer.api

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import io.element.android.libraries.matrix.api.core.RoomIdOrAlias
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf

/**
 * The known display names of the users and rooms a [MentionNodeContent] can point to, used to replace
 * the raw handles in the mention pills with something more readable.
 *
 * This instance is meant to be stable for the whole screen: the display names are held as [State] so they can
 * change without providing a new [LocalMentionDisplayNames], and only the composables whose resolved display
 * texts changed will be recomposed (see [displayTextFor]).
 *
 * @param users the display names of the room members, by user id.
 * @param rooms the room names, by room id or alias.
 * @param userStatuses the emoji of the displayed status of the room members (i.e. 🎧 when in a call), by user id.
 */
@Stable
class MentionDisplayNames(
    private val users: State<ImmutableMap<UserId, String>>,
    private val rooms: State<ImmutableMap<RoomIdOrAlias, String>>,
    private val userStatuses: State<ImmutableMap<UserId, String>> = mutableStateOf(persistentMapOf()),
) {
    /**
     * Creates an instance with fixed display names, i.e. for previews and tests.
     */
    constructor(
        users: ImmutableMap<UserId, String> = persistentMapOf(),
        rooms: ImmutableMap<RoomIdOrAlias, String> = persistentMapOf(),
        userStatuses: ImmutableMap<UserId, String> = persistentMapOf(),
    ) : this(users = mutableStateOf(users), rooms = mutableStateOf(rooms), userStatuses = mutableStateOf(userStatuses))

    /**
     * Returns the text to display in the pill for [mention]: the user or room display name if known,
     * or the original [MentionNodeContent.displayText] otherwise.
     *
     * This reads snapshot state: call it from a `derivedStateOf` block so readers are only notified when the result changes.
     */
    fun displayTextFor(mention: MentionNodeContent): String = when (mention) {
        is MentionNodeContent.User -> users.value[mention.userId]?.let { "@$it" }
        is MentionNodeContent.Room -> rooms.value[mention.roomIdOrAlias]?.let { "#$it" }
        is MentionNodeContent.Everyone -> null
    } ?: mention.displayText

    /**
     * Returns the full text of the pill for [mention]: its [displayTextFor] followed by the user status emoji, if any.
     *
     * This reads snapshot state: call it from a `derivedStateOf` block so readers are only notified when the result changes.
     */
    fun pillTextFor(mention: MentionNodeContent): String {
        val displayText = displayTextFor(mention)
        val status = (mention as? MentionNodeContent.User)?.let { userStatuses.value[it.userId] }
        return if (status != null) "$displayText $status" else displayText
    }
}

/**
 * Provides the [MentionDisplayNames] used to render the mention pills. It's empty by default, so the
 * pills will display their original [MentionNodeContent.displayText].
 *
 * The provided value is not expected to change, the display names it contains are observable state instead.
 */
val LocalMentionDisplayNames = staticCompositionLocalOf { MentionDisplayNames() }
