/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.components.preferences

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.element.android.compound.theme.ElementSpacing
import io.element.android.libraries.designsystem.preview.ElementThemedPreview
import io.element.android.libraries.designsystem.preview.PreviewGroup

@Composable
fun PreferenceDivider(
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.height(ElementSpacing.l))
}

@Preview(group = PreviewGroup.Preferences)
@Composable
internal fun PreferenceDividerPreview() = ElementThemedPreview {
    PreferenceDivider()
}
