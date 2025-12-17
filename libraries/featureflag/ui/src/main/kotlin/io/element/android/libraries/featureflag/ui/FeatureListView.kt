/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.featureflag.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.components.preferences.PreferenceCheckbox
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.featureflag.ui.model.aFeatureUiModelList
import kotlinx.collections.immutable.ImmutableList

@Composable
fun FeatureListView(
    features: ImmutableList<FeatureUiModel>,
    onCheckedChange: (FeatureUiModel, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
    ) {
        features.forEach { feature ->
            fun onCheckedChange(isChecked: Boolean) {
                onCheckedChange(feature, isChecked)
            }

            FeaturePreferenceView(feature = feature, onCheckedChange = ::onCheckedChange)
        }
    }
}

@Composable
private fun FeaturePreferenceView(
    feature: FeatureUiModel,
    onCheckedChange: (Boolean) -> Unit,
) {
    PreferenceCheckbox(
        title = feature.title,
        supportingText = feature.description,
        isChecked = feature.isEnabled,
        onCheckedChange = onCheckedChange
    )
}

@PreviewsDayNight
@Composable
internal fun FeatureListViewPreview() = ElementPreview {
    FeatureListView(
        features = aFeatureUiModelList(),
        onCheckedChange = { _, _ -> }
    )
}
