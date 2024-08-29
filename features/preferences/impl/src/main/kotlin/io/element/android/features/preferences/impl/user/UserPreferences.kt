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
import androidx.compose.ui.tooling.preview.PreviewParameter
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.MatrixUserHeader
import io.element.android.libraries.matrix.ui.components.MatrixUserWithNullProvider

@Composable
fun UserPreferences(
    isDebugBuild: Boolean,
    user: MatrixUser?,
    modifier: Modifier = Modifier,
) {
    MatrixUserHeader(
        isDebugBuild = isDebugBuild,
        modifier = modifier,
        matrixUser = user
    )
}

@PreviewsDayNight
@Composable
internal fun UserPreferencesPreview(@PreviewParameter(MatrixUserWithNullProvider::class) matrixUser: MatrixUser?) = ElementPreview {
    UserPreferences(isDebugBuild = false, matrixUser)
}
