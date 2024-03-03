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
import io.element.android.libraries.architecture.AsyncAction
import kotlinx.collections.immutable.ImmutableList

data class AdvancedSettingsState(
    val isDeveloperModeEnabled: Boolean,
    val isSharePresenceEnabled: Boolean,
    val isReactionPickerSearchEnabled: Boolean,
    val theme: Theme,
    val showChangeThemeDialog: Boolean,
    val currentPushDistributor: AsyncAction<String>,
    val availablePushDistributors: ImmutableList<String>,
    val showChangePushProviderDialog: Boolean,
    val eventSink: (AdvancedSettingsEvents) -> Unit
)
