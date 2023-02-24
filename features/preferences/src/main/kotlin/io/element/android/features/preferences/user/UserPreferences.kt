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

package io.element.android.features.preferences.user

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import io.element.android.libraries.architecture.Async
import io.element.android.libraries.designsystem.preview.ElementPreviewDark
import io.element.android.libraries.designsystem.preview.ElementPreviewLight
import io.element.android.libraries.matrix.ui.components.MatrixUserHeader
import io.element.android.libraries.matrix.ui.components.MatrixUserWithNullProvider
import io.element.android.libraries.matrix.ui.model.MatrixUser

@Composable
fun UserPreferences(
    user: Async<MatrixUser>,
    modifier: Modifier = Modifier,
) {
    when (val userData = user.dataOrNull()) {
        null -> Spacer(modifier = modifier.height(1.dp))
        else -> MatrixUserHeader(
            modifier = modifier,
            matrixUser = userData
        )
    }
}

@Preview
@Composable
internal fun UserPreferencesLightPreview(@PreviewParameter(MatrixUserWithNullProvider::class) matrixUser: MatrixUser?) =
    ElementPreviewLight { ContentToPreview(matrixUser) }

@Preview
@Composable
internal fun UserPreferencesDarkPreview(@PreviewParameter(MatrixUserWithNullProvider::class) matrixUser: MatrixUser?) =
    ElementPreviewDark { ContentToPreview(matrixUser) }

@Composable
private fun ContentToPreview(matrixUser: MatrixUser?) {
    if (matrixUser == null) {
        UserPreferences(Async.Uninitialized)
    } else {
        UserPreferences(Async.Success(matrixUser))
    }
}
