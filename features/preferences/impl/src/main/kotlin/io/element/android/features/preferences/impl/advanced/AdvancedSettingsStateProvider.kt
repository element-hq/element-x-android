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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.compound.theme.Theme
import io.element.android.libraries.architecture.AsyncAction
import kotlinx.collections.immutable.toImmutableList

open class AdvancedSettingsStateProvider : PreviewParameterProvider<AdvancedSettingsState> {
    override val values: Sequence<AdvancedSettingsState>
        get() = sequenceOf(
            aAdvancedSettingsState(),
            aAdvancedSettingsState(isDeveloperModeEnabled = true),
            aAdvancedSettingsState(showChangeThemeDialog = true),
            aAdvancedSettingsState(isSendPublicReadReceiptsEnabled = true),
            aAdvancedSettingsState(showChangePushProviderDialog = true),
            aAdvancedSettingsState(pushDistributor = AsyncAction.Loading),
            aAdvancedSettingsState(pushDistributor = AsyncAction.Failure(Exception("Failed to change distributor"))),
        )
}

fun aAdvancedSettingsState(
    isDeveloperModeEnabled: Boolean = false,
    isSendPublicReadReceiptsEnabled: Boolean = false,
    showChangeThemeDialog: Boolean = false,
    pushDistributor: AsyncAction<String> = AsyncAction.Success("Firebase"),
    pushDistributors: List<String> = listOf("Firebase", "ntfy"),
    showChangePushProviderDialog: Boolean = false,
) = AdvancedSettingsState(
    isDeveloperModeEnabled = isDeveloperModeEnabled,
    isSharePresenceEnabled = isSendPublicReadReceiptsEnabled,
    theme = Theme.System,
    showChangeThemeDialog = showChangeThemeDialog,
    pushDistributor = pushDistributor,
    pushDistributors = pushDistributors.toImmutableList(),
    showChangePushProviderDialog = showChangePushProviderDialog,
    eventSink = {}
)
