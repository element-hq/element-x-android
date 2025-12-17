/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup
import io.element.android.libraries.designsystem.theme.components.HorizontalDivider

@Composable
fun PreferenceDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(modifier = modifier)
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceDividerPreview() = ElementThemedPreview {
    PreferenceDivider()
}
