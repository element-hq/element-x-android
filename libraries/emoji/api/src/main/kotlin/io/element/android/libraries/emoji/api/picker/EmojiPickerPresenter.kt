/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.api.picker

import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.emoji.api.recentemojis.GetRecentEmojis

fun interface EmojiPickerPresenter : Presenter<EmojiPickerState> {
    fun interface Factory {
        fun create(getRecentEmojis: GetRecentEmojis): EmojiPickerPresenter
    }
}
