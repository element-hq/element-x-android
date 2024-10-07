/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.textcomposer.model

import io.element.android.libraries.matrix.api.room.IntentionalMention

data class Message(
    val html: String?,
    val markdown: String,
    val intentionalMentions: List<IntentionalMention>,
)
