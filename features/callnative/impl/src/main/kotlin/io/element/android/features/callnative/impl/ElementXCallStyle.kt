/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.element.android.call.ui.theme.ElementCallColors
import io.element.android.call.ui.theme.ElementCallIcons
import io.element.android.call.ui.theme.ElementCallStyle
import io.element.android.call.ui.theme.ElementCallTypography
import io.element.android.libraries.designsystem.components.avatar.Avatar
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.components.avatar.AvatarSize
import io.element.android.libraries.designsystem.components.avatar.AvatarType

/**
 * How Element X dresses the native call.
 *
 * Deliberately almost nothing. The component's own defaults are already Compound - its colours are
 * Compound's dark palette and its glyphs are Compound vectors - so re-deriving them from
 * `ElementTheme` would buy no visual change and a lot of mapping to keep in step. The call also stays
 * dark in a light-themed app on purpose: it is a video surface, as it is in Element Call on the web.
 *
 * What the component genuinely cannot do is turn an `mxc://` URL into a picture, because that needs a
 * Matrix media loader. So the avatar is the one thing overridden here, and it is what makes faces
 * appear on the tiles rather than initials.
 */
@Composable
fun rememberElementXCallStyle(): ElementCallStyle {
    val icons = ElementCallIcons.default()
    return remember(icons) {
        ElementCallStyle(
            colors = ElementCallColors.Dark,
            typography = ElementCallTypography.Default,
            icons = icons,
            avatar = { data, size ->
                Avatar(
                    avatarData = AvatarData(
                        id = data.id,
                        name = data.name,
                        url = data.url,
                        // Unused: forcedAvatarSize below is what decides the size, and the component
                        // asks in dp because it lays the tiles out itself.
                        size = AvatarSize.ActiveCallItem,
                    ),
                    avatarType = AvatarType.User,
                    forcedAvatarSize = size,
                )
            },
        )
    }
}
