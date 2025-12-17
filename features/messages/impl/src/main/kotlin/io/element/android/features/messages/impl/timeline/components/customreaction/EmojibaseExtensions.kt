/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.EmojiFlags
import androidx.compose.material.icons.outlined.EmojiFoodBeverage
import androidx.compose.material.icons.outlined.EmojiNature
import androidx.compose.material.icons.outlined.EmojiObjects
import androidx.compose.material.icons.outlined.EmojiPeople
import androidx.compose.material.icons.outlined.EmojiSymbols
import androidx.compose.material.icons.outlined.EmojiTransportation
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.emojibasebindings.EmojibaseCategory
import io.element.android.features.messages.impl.R

@get:StringRes
val EmojibaseCategory.title: Int
    get() = when (this) {
        EmojibaseCategory.People -> R.string.emoji_picker_category_people
        EmojibaseCategory.Nature -> R.string.emoji_picker_category_nature
        EmojibaseCategory.Foods -> R.string.emoji_picker_category_foods
        EmojibaseCategory.Activity -> R.string.emoji_picker_category_activity
        EmojibaseCategory.Places -> R.string.emoji_picker_category_places
        EmojibaseCategory.Objects -> R.string.emoji_picker_category_objects
        EmojibaseCategory.Symbols -> R.string.emoji_picker_category_symbols
        EmojibaseCategory.Flags -> R.string.emoji_picker_category_flags
    }

val EmojibaseCategory.icon: ImageVector
    get() = when (this) {
        EmojibaseCategory.People -> Icons.Outlined.EmojiPeople
        EmojibaseCategory.Nature -> Icons.Outlined.EmojiNature
        EmojibaseCategory.Foods -> Icons.Outlined.EmojiFoodBeverage
        EmojibaseCategory.Activity -> Icons.Outlined.EmojiEvents
        EmojibaseCategory.Places -> Icons.Outlined.EmojiTransportation
        EmojibaseCategory.Objects -> Icons.Outlined.EmojiObjects
        EmojibaseCategory.Symbols -> Icons.Outlined.EmojiSymbols
        EmojibaseCategory.Flags -> Icons.Outlined.EmojiFlags
    }
