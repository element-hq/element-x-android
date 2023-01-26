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

package io.element.android.libraries.designsystem.theme

import io.element.android.libraries.designsystem.SystemGrey4Dark
import io.element.android.libraries.designsystem.SystemGrey6Light

/**
 * Room list
 */
val ElementColors.roomListRoomName get() = primary
val ElementColors.roomListRoomMessage get() = secondary
val ElementColors.roomListRoomMessageDate get() = secondary
val ElementColors.roomListUnreadIndicator get() = primary
val ElementColors.roomListPlaceHolder get() = if (isLight) SystemGrey6Light else SystemGrey4Dark
