/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.labs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.element.android.compound.tokens.generated.CompoundIcons
import io.element.android.features.preferences.impl.R
import io.element.android.libraries.designsystem.atomic.molecules.IconTitleSubtitleMolecule
import io.element.android.libraries.designsystem.atomic.pages.HeaderFooterPage
import io.element.android.libraries.designsystem.components.BigIcon
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.components.list.ListItemContent
import io.element.android.libraries.designsystem.components.list.SwitchListItem
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.IconSource
import io.element.android.libraries.designsystem.theme.components.TopAppBar
import io.element.android.libraries.featureflag.ui.model.FeatureUiModel
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabsView(
    state: LabsState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HeaderFooterPage(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding(),
        topBar = {
            TopAppBar(
                titleStr = stringResource(R.string.screen_labs_title),
                navigationIcon = {
                    BackButton(onClick = onBack)
                }
            )
        },
        header = {
            IconTitleSubtitleMolecule(
                modifier = Modifier.padding(horizontal = 24.dp),
                title = stringResource(R.string.screen_labs_header_title),
                subTitle = stringResource(R.string.screen_labs_header_description),
                iconStyle = BigIcon.Style.Default(CompoundIcons.Labs())
            )
        },
        contentPadding = PaddingValues(),
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 20.dp),
            ) {
                items(items = state.features, key = { it.key }) { feature ->
                    SwitchListItem(
                        leadingContent = feature.icon?.let { ListItemContent.Icon(it) },
                        headline = feature.title,
                        supportingText = feature.description,
                        value = feature.isEnabled,
                        onChange = {
                            state.eventSink(LabsEvents.ToggleFeature(feature.key))
                        }
                    )
                }
            }
        }
    )
}

@PreviewsDayNight
@Composable
internal fun LabsViewPreview() {
    ElementPreview {
        LabsView(
            state = LabsState(
                features = persistentListOf(
                    FeatureUiModel(
                        key = "feature_1",
                        title = "Feature 1",
                        description = "This is a description of feature 1.",
                        isEnabled = true,
                        icon = IconSource.Vector(CompoundIcons.Threads()),
                    ),
                    FeatureUiModel(
                        key = "feature_2",
                        title = "Feature 2",
                        description = "This is a description of feature 2.",
                        isEnabled = false,
                        icon = IconSource.Vector(CompoundIcons.VideoCall()),
                    )
                ),
                eventSink = {},
            ),
            onBack = {},
        )
    }
}
