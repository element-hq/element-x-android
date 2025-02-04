/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class AvatarDataProvider : PreviewParameterProvider<AvatarData> {
    override val values: Sequence<AvatarData>
        get() = AvatarSize.values()
            .asSequence()
            .map {
                sequenceOf(
                    anAvatarData(size = it),
                    anAvatarData(size = it).copy(name = null),
                    anAvatarData(size = it).copy(url = "aUrl"),
                )
            }
            .flatten()
}

fun anAvatarData(
    // Let's the id not start with a 'a'.
    id: String = "@id_of_alice:server.org",
    name: String = "Alice",
    size: AvatarSize = AvatarSize.RoomListItem,
) = AvatarData(
    id = id,
    name = name,
    size = size,
)
