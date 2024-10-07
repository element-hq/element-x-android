/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
        PreferenceText(
            title = stringResource(id = CommonStrings.common_open_source_licenses),
            onClick = onOpenSourceLicensesClick,
        )
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
