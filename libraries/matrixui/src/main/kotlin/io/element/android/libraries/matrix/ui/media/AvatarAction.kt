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

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import io.element.android.libraries.designsystem.icons.CompoundDrawables
import io.element.android.libraries.ui.strings.CommonStrings

@Immutable
sealed class AvatarAction(
    @StringRes val titleResId: Int,
    @DrawableRes val iconResourceId: Int,
    val destructive: Boolean = false,
) {
    data object TakePhoto : AvatarAction(
        titleResId = CommonStrings.action_take_photo,
        iconResourceId = CompoundDrawables.ic_compound_take_photo,
    )

    data object ChoosePhoto : AvatarAction(
        titleResId = CommonStrings.action_choose_photo,
        iconResourceId = CompoundDrawables.ic_compound_image,
    )

    data object Remove : AvatarAction(
        titleResId = CommonStrings.action_remove,
        iconResourceId = CompoundDrawables.ic_compound_delete,
        destructive = true
    )
}
