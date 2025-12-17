/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl.details

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.element.android.features.licenses.impl.list.aDependencyLicenseItem
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.libraries.designsystem.components.ClickableLinkText
import io.element.android.libraries.designsystem.components.button.BackButton
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.ListItem
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.theme.components.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesDetailsView(
    licenseItem: DependencyLicenseItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                titleStr = licenseItem.safeName,
                navigationIcon = { BackButton(onClick = onBack) },
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.padding(contentPadding),
        ) {
            val licenses = licenseItem.licenses.orEmpty() +
                licenseItem.unknownLicenses.orEmpty()
            items(licenses) { license ->
                val text = buildString {
                    if (license.name != null) {
                        append(license.name)
                        append("\n")
                        append("\n")
                    }
                    if (license.url != null) {
                        append(license.url)
                    }
                }
                ListItem(
                    headlineContent = {
                        ClickableLinkText(
                            text = text,
                            interactionSource = remember { MutableInteractionSource() },
                        )
                    }
                )
            }
        }
    }
}

@PreviewsDayNight
@Composable
internal fun DependenciesDetailsViewPreview() = ElementPreview {
    DependenciesDetailsView(
        licenseItem = aDependencyLicenseItem(),
        onBack = {}
    )
}
