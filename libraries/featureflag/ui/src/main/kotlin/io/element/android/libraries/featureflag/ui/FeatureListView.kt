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

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.components.preferences.PreferenceSwitch
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import io.element.android.libraries.featureflag.ui.model.aFeatureUiModelList
import kotlinx.collections.immutable.ImmutableList

@Composable
fun FeatureListView(
    features: ImmutableList<FeatureUiModel>,
    onCheckedChange: (FeatureUiModel, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(
            items = features,
            key = { it.key }
        ) { feature ->

            fun onCheckedChange(isChecked: Boolean) {
                onCheckedChange(feature, isChecked)
            }

            FeaturePreferenceView(feature = feature, onCheckedChange = ::onCheckedChange)
        }
    }
}

@Composable
fun FeaturePreferenceView(
    feature: FeatureUiModel,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    PreferenceSwitch(
        title = feature.title,
        isChecked = feature.isEnabled,
        modifier = modifier,
        onCheckedChange = onCheckedChange
    )
}

@Preview
@Composable
internal fun FeatureListViewLightPreview() =
    ElementPreviewLight { ContentToPreview() }

@Preview
@Composable
internal fun FeatureListViewDarkPreview() =
    ElementPreviewDark { ContentToPreview() }

@Composable
private fun ContentToPreview() {
    FeatureListView(
        features = aFeatureUiModelList(),
        onCheckedChange = { _, _ -> }
    )
}
