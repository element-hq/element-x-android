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

package io.element.android.libraries.matrix.ui.media

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import io.element.android.libraries.ui.strings.R

@Immutable
sealed class AvatarAction(
    @StringRes val titleResId: Int,
    val icon: ImageVector,
    val destructive: Boolean = false,
) {
    object TakePhoto : AvatarAction(titleResId = R.string.action_take_photo, icon = Icons.Outlined.PhotoCamera)
    object ChoosePhoto : AvatarAction(titleResId = R.string.action_choose_photo, icon = Icons.Outlined.PhotoLibrary)
    object Remove : AvatarAction(titleResId = R.string.action_remove, icon = Icons.Outlined.Delete, destructive = true)
}
