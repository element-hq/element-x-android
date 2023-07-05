/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.features.preferences.impl.user

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.element.android.libraries.designsystem.preview.ElementPreviews
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
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

@ElementPreviews
@Composable
internal fun UserPreferencesLightPreview(@PreviewParameter(MatrixUserWithNullProvider::class) matrixUser: MatrixUser?) {
    ElementPreview { ContentToPreview(matrixUser) }
}

@ElementPreviews
@Composable
internal fun UserPreferencesDarkPreview(@PreviewParameter(MatrixUserWithNullProvider::class) matrixUser: MatrixUser?) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser?) {
    UserPreferences(matrixUser)
}
