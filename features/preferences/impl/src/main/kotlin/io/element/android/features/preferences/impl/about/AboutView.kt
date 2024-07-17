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

package io.element.android.features.preferences.impl.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.components.preferences.PreferencePage
import io.element.android.libraries.designsystem.components.preferences.PreferenceText
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.ui.strings.CommonStrings

@Composable
fun AboutView(
    state: AboutState,
    onElementLegalClick: (ElementLegal) -> Unit,
    onOpenSourceLicensesClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PreferencePage(
        modifier = modifier,
        onBackClick = onBackClick,
        title = stringResource(id = CommonStrings.common_about)
    ) {
        state.elementLegals.forEach { elementLegal ->
            PreferenceText(
                title = stringResource(id = elementLegal.titleRes),
                onClick = { onElementLegalClick(elementLegal) }
            )
        }
        if (state.hasOpenSourcesLicenses) {
            PreferenceText(
                title = stringResource(id = CommonStrings.common_open_source_licenses),
                onClick = onOpenSourceLicensesClick,
            )
        }
    }
}

@PreviewsDayNight
@Composable
internal fun AboutViewPreview(@PreviewParameter(AboutStateProvider::class) state: AboutState) = ElementPreview {
    AboutView(
        state = state,
        onElementLegalClick = {},
        onOpenSourceLicensesClick = {},
        onBackClick = {},
    )
}
