/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import io.element.android.compound.tokens.generated.CompoundIcons

enum class HomeNavigationBarItem(
    @StringRes
    val labelRes: Int,
) {
    Chats(
        labelRes = R.string.screen_roomlist_main_space_title
    ),
    Spaces(
        // TODO Create a new entry in Localazy
        labelRes = R.string.screen_roomlist_main_space_title
    );

    @Composable
    fun icon() = when (this) {
        Chats -> CompoundIcons.ChatSolid()
        // TODO Spaces -> CompoundIcons.Workspace()
        Spaces -> CompoundIcons.Code()
    }

    companion object {
        fun from(index: Int): HomeNavigationBarItem {
            return entries.getOrElse(index) { Chats }
        }
    }
}
