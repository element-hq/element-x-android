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

package io.element.android.appconfig

object RoomListConfig {
    const val SHOW_INVITE_MENU_ITEM = false
    const val SHOW_REPORT_PROBLEM_MENU_ITEM = false

    const val HAS_DROP_DOWN_MENU = SHOW_INVITE_MENU_ITEM || SHOW_REPORT_PROBLEM_MENU_ITEM
}
