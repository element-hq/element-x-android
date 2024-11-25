/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.element.android.features.createroom.impl.R
import io.element.android.libraries.designsystem.icons.CompoundDrawables

enum class RoomVisibilityItem(
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    @StringRes val description: Int
) {
    Private(
        icon = CompoundDrawables.ic_compound_lock,
        title = R.string.screen_create_room_private_option_title,
        description = R.string.screen_create_room_private_option_description,
    ),
    Public(
        icon = CompoundDrawables.ic_compound_public,
        title = R.string.screen_create_room_public_option_title,
        description = R.string.screen_create_room_public_option_description,
    )
}
