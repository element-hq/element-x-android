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
