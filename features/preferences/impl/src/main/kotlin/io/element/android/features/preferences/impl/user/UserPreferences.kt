/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2022-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserHeader
import io.element.android.libraries.matrix.ui.components.MatrixUserWithNullProvider

@Composable
fun UserPreferences(
    user: MatrixUser?,
    modifier: Modifier = Modifier,
) {
    MatrixUserHeader(
        modifier = modifier,
        matrixUser = user
    )
}

@PreviewsDayNight
@Composable
internal fun UserPreferencesPreview(@PreviewParameter(MatrixUserWithNullProvider::class) matrixUser: MatrixUser?) = ElementPreview {
    UserPreferences(matrixUser)
}
