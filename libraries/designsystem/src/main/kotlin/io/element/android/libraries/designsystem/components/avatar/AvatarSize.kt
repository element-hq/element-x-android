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

package io.element.android.libraries.designsystem.components.avatar

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

sealed class AvatarSize(open val dp: Dp) {

    object SMALL : AvatarSize(32.dp)
    object MEDIUM : AvatarSize(40.dp)
    object BIG : AvatarSize(48.dp)
    object HUGE : AvatarSize(96.dp)

    // FIXME maybe remove this field and switch back to an enum (or remove this class) when design system will be integrated
    data class Custom(override val dp: Dp) : AvatarSize(dp)
}
