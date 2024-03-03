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

package io.element.android.features.preferences.impl.advanced

import io.element.android.compound.theme.Theme

sealed interface AdvancedSettingsEvents {
    data class SetDeveloperModeEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data class SetSharePresenceEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data class SetReactionPickerSearchEnabled(val enabled: Boolean) : AdvancedSettingsEvents
    data object ChangeTheme : AdvancedSettingsEvents
    data object CancelChangeTheme : AdvancedSettingsEvents
    data class SetTheme(val theme: Theme) : AdvancedSettingsEvents
    data object ChangePushProvider : AdvancedSettingsEvents
    data object CancelChangePushProvider : AdvancedSettingsEvents
    data class SetPushProvider(val index: Int) : AdvancedSettingsEvents
}
