/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.configureroom

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.createroom.impl.R
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

data class RoomPrivacyItem(
    val privacy: RoomPrivacy,
    @DrawableRes val icon: Int,
    val title: String,
    val description: String,
)

@Composable
fun roomPrivacyItems(): ImmutableList<RoomPrivacyItem> {
    return RoomPrivacy.entries
        .map {
            when (it) {
                RoomPrivacy.Private -> RoomPrivacyItem(
                    privacy = it,
                    icon = CompoundDrawables.ic_compound_lock_solid,
                    title = stringResource(R.string.screen_create_room_private_option_title),
                    description = stringResource(R.string.screen_create_room_private_option_description),
                )
                RoomPrivacy.Public -> RoomPrivacyItem(
                    privacy = it,
                    icon = CompoundDrawables.ic_compound_public,
                    title = stringResource(R.string.screen_create_room_public_option_title),
                    description = stringResource(R.string.screen_create_room_public_option_description),
                )
            }
        }
        .toImmutableList()
}
