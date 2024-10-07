/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room

import io.element.android.libraries.matrix.api.room.IntentionalMention
import org.matrix.rustcomponents.sdk.Mentions

fun List<IntentionalMention>.map(): Mentions {
    val hasRoom = any { it is IntentionalMention.Room }
    val userIds = filterIsInstance<IntentionalMention.User>().map { it.userId.value }
    return Mentions(userIds, hasRoom)
}
