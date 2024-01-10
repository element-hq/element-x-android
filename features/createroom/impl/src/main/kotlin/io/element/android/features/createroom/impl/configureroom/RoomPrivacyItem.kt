/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    return RoomPrivacy.values()
        .map {
            when (it) {
                RoomPrivacy.Private -> RoomPrivacyItem(
                    privacy = it,
                    icon = CompoundDrawables.ic_lock,
                    title = stringResource(R.string.screen_create_room_private_option_title),
                    description = stringResource(R.string.screen_create_room_private_option_description),
                )
                RoomPrivacy.Public -> RoomPrivacyItem(
                    privacy = it,
                    icon = CompoundDrawables.ic_public,
                    title = stringResource(R.string.screen_create_room_public_option_title),
                    description = stringResource(R.string.screen_create_room_public_option_description),
                )
            }
        }
        .toImmutableList()
}
