/*
 * Copyright 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.alpha

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.home.impl.R

internal enum class AlphaTab(
    @StringRes val labelRes: Int,
    val icon: @Composable () -> ImageVector,
) {
    Messages(R.string.screen_alpha_tab_messages, { CompoundIcons.Chat() }),
    Contacts(R.string.screen_alpha_tab_contacts, { CompoundIcons.Group() }),
    Discover(R.string.screen_alpha_tab_discover, { CompoundIcons.Search() }),
    Me(R.string.screen_alpha_tab_me, { CompoundIcons.UserSolid() }),
}
