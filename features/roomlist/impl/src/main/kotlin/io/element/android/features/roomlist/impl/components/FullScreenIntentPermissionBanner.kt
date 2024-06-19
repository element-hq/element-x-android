/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.roomlist.impl.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.features.roomlist.impl.R
import io.element.android.features.roomlist.impl.aFullScreenIntentPermissionsState
import io.element.android.libraries.designsystem.atomic.molecules.DialogLikeBannerMolecule
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsState

@Composable
fun FullScreenIntentPermissionBanner(state: FullScreenIntentPermissionsState) {
    DialogLikeBannerMolecule(
        title = stringResource(R.string.full_screen_intent_banner_title),
        content = stringResource(R.string.full_screen_intent_banner_message),
        onDismissClick = state.dismissFullScreenIntentBanner,
        onSubmitClick = state.openFullScreenIntentSettings,
    )
}

@PreviewsDayNight
@Composable
internal fun FullScreenIntentPermissionBannerPreview() {
    ElementPreview {
        FullScreenIntentPermissionBanner(aFullScreenIntentPermissionsState())
    }
}
